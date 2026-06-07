package com.yinyu.service.impl;

import com.wf.captcha.SpecCaptcha;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.UserAdminPasswordResetRequest;
import com.yinyu.entity.dto.UserAdminQueryRequest;
import com.yinyu.entity.dto.UserAdminSaveRequest;
import com.yinyu.entity.dto.UserAdminStatusRequest;
import com.yinyu.entity.dto.UserLoginRequest;
import com.yinyu.entity.dto.UserPasswordUpdateRequest;
import com.yinyu.entity.dto.UserPlaylistActionRequest;
import com.yinyu.entity.dto.UserProfileUpdateRequest;
import com.yinyu.entity.dto.UserRegisterRequest;
import com.yinyu.entity.dto.UserSongActionRequest;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.enums.UserActionTypeEnum;
import com.yinyu.entity.po.Playlist;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.po.User;
import com.yinyu.entity.po.UserAction;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.PlaylistVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.entity.vo.UserCaptchaVO;
import com.yinyu.entity.vo.UserAdminVO;
import com.yinyu.entity.vo.UserLibraryVO;
import com.yinyu.entity.vo.UserVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.PlaylistMapper;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.mapper.UserActionMapper;
import com.yinyu.mapper.UserMapper;
import com.yinyu.service.DictService;
import com.yinyu.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String SESSION_CAPTCHA_KEY = "web:user:captcha";
    private static final String SESSION_USER_ID_KEY = "web:user:id";
    private static final int LIBRARY_LIMIT = 50;

    private final UserMapper userMapper;
    private final UserActionMapper userActionMapper;
    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final PlaylistMapper playlistMapper;
    private final DictService dictService;

    public UserServiceImpl(UserMapper userMapper, UserActionMapper userActionMapper, SongMapper songMapper, SingerMapper singerMapper, PlaylistMapper playlistMapper, DictService dictService) {
        this.userMapper = userMapper;
        this.userActionMapper = userActionMapper;
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.playlistMapper = playlistMapper;
        this.dictService = dictService;
    }

    @Override
    public ListData<UserAdminVO> listAdminPage(UserAdminQueryRequest request) {
        UserAdminQueryRequest query = request == null ? new UserAdminQueryRequest() : request;
        return new ListData<>(userMapper.selectAdminPage(query), userMapper.countAdminPage(query));
    }

    @Override
    public UserAdminVO getAdminDetail(Long id) {
        requireUser(id);
        UserAdminVO detail = userMapper.selectAdminDetail(id);
        if (detail == null) {
            throw new BusinessException("user not found");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdmin(UserAdminSaveRequest request) {
        User user = requireUser(request.getId());
        validateProfileUnique(request.getNickname(), request.getEmail(), user.getId());
        user.setNickname(request.getNickname().trim());
        user.setAvatar(trimToNull(request.getAvatar()));
        user.setGender(trimToNull(request.getGender()));
        user.setEmail(trimToNull(request.getEmail()));
        user.setSignature(trimToNull(request.getSignature()));
        user.setStatus(normalizeStatus(request.getStatus()));
        user.setRemark(trimToNull(request.getRemark()));
        userMapper.updateAdmin(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminStatus(UserAdminStatusRequest request) {
        String status = normalizeStatus(request.getStatus());
        request.getIds().forEach(this::requireUser);
        userMapper.updateStatus(request.getIds(), status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetAdminPassword(UserAdminPasswordResetRequest request) {
        if (request.getNewPassword().trim().length() < 6) {
            throw new BusinessException("password length must be at least 6");
        }
        User user = requireUser(request.getId());
        userMapper.updatePassword(user.getId(), md5(request.getNewPassword().trim()));
    }

    @Override
    public UserCaptchaVO generateCaptcha(HttpSession session) {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text().toLowerCase();
        session.setAttribute(SESSION_CAPTCHA_KEY, code);
        UserCaptchaVO vo = new UserCaptchaVO();
        vo.setImageBase64(captcha.toBase64());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterRequest request, HttpSession session) {
        validateCaptcha(request.getCaptchaCode(), session);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("confirm password is not consistent");
        }
        if (request.getPassword().trim().length() < 6) {
            throw new BusinessException("password length must be at least 6");
        }
        validateUserUnique(request.getNickname(), request.getEmail());

        User entity = new User();
        entity.setPassword(md5(request.getPassword().trim()));
        entity.setNickname(request.getNickname().trim());
        entity.setEmail(trimToNull(request.getEmail()));
        entity.setStatus("enabled");
        userMapper.insert(entity);
        session.setAttribute(SESSION_USER_ID_KEY, entity.getId());
        return toVO(requireEnabledUser(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO login(UserLoginRequest request, HttpSession session) {
        validateCaptcha(request.getCaptchaCode(), session);
        User user = userMapper.selectByLoginName(request.getAccount().trim());
        if (user == null) {
            throw new BusinessException(401, "account or password is invalid");
        }
        if (!"enabled".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(403, "user is disabled");
        }
        if (!user.getPassword().equals(md5(request.getPassword().trim()))) {
            throw new BusinessException(401, "account or password is invalid");
        }
        userMapper.updateLastLoginTime(user.getId());
        session.setAttribute(SESSION_USER_ID_KEY, user.getId());
        return toVO(requireEnabledUser(user.getId()));
    }

    @Override
    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_ID_KEY);
    }

    @Override
    public UserVO getCurrentUser(HttpSession session) {
        Long userId = getCurrentUserId(session, false);
        if (userId == null) {
            return null;
        }
        return toVO(requireEnabledUser(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(UserProfileUpdateRequest request, HttpSession session) {
        User user = requireLoginUser(session);
        validateProfileUnique(request.getNickname(), request.getEmail(), user.getId());
        user.setNickname(request.getNickname().trim());
        user.setEmail(trimToNull(request.getEmail()));
        user.setGender(trimToNull(request.getGender()));
        user.setAvatar(trimToNull(request.getAvatar()));
        user.setSignature(trimToNull(request.getSignature()));
        userMapper.updateProfile(user);
        return toVO(requireEnabledUser(user.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordUpdateRequest request, HttpSession session) {
        User user = requireLoginUser(session);
        if (!md5(request.getOldPassword().trim()).equals(user.getPassword())) {
            throw new BusinessException("old password is invalid");
        }
        if (!request.getNewPassword().trim().equals(request.getConfirmPassword().trim())) {
            throw new BusinessException("confirm password is not consistent");
        }
        if (request.getNewPassword().trim().length() < 6) {
            throw new BusinessException("password length must be at least 6");
        }
        if (request.getNewPassword().trim().equals(request.getOldPassword().trim())) {
            throw new BusinessException("new password can not be the same as old password");
        }
        userMapper.updatePassword(user.getId(), md5(request.getNewPassword().trim()));
    }

    @Override
    public UserLibraryVO getLibrary(HttpSession session) {
        User user = requireLoginUser(session);
        UserLibraryVO vo = new UserLibraryVO();
        vo.setUser(toVO(user));
        vo.setLikeSongCount(countAction(user.getId(), UserActionTypeEnum.LIKE_SONG));
        vo.setFavoritePlaylistCount(countAction(user.getId(), UserActionTypeEnum.FAVORITE_PLAYLIST));
        vo.setPlayHistoryCount(countAction(user.getId(), UserActionTypeEnum.PLAY_SONG));
        vo.setLikedSongs(loadSongsByAction(user.getId(), UserActionTypeEnum.LIKE_SONG));
        vo.setRecentSongs(loadSongsByAction(user.getId(), UserActionTypeEnum.PLAY_SONG));
        vo.setFavoritePlaylists(loadPlaylistsByAction(user.getId(), UserActionTypeEnum.FAVORITE_PLAYLIST));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeSong(UserSongActionRequest request, HttpSession session) {
        User user = requireLoginUser(session);
        requireSong(request.getSongId());
        String actionType = UserActionTypeEnum.LIKE_SONG.getCode();
        UserAction action = userActionMapper.selectByUserIdAndTypeAndTargetId(user.getId(), actionType, request.getSongId());
        if (action != null) {
            return;
        }
        insertAction(user.getId(), actionType, request.getSongId(), request.getProgressSeconds());
        songMapper.updateLikeCount(request.getSongId(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeSong(Long songId, HttpSession session) {
        User user = requireLoginUser(session);
        requireSong(songId);
        int deleted = userActionMapper.deleteByUserIdAndTypeAndTargetId(user.getId(), UserActionTypeEnum.LIKE_SONG.getCode(), songId);
        if (deleted > 0) {
            songMapper.updateLikeCount(songId, -1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoritePlaylist(UserPlaylistActionRequest request, HttpSession session) {
        User user = requireLoginUser(session);
        requirePlaylist(request.getPlaylistId());
        String actionType = UserActionTypeEnum.FAVORITE_PLAYLIST.getCode();
        UserAction action = userActionMapper.selectByUserIdAndTypeAndTargetId(user.getId(), actionType, request.getPlaylistId());
        if (action != null) {
            return;
        }
        insertAction(user.getId(), actionType, request.getPlaylistId(), null);
        playlistMapper.updateFavoriteCount(request.getPlaylistId(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavoritePlaylist(Long playlistId, HttpSession session) {
        User user = requireLoginUser(session);
        requirePlaylist(playlistId);
        int deleted = userActionMapper.deleteByUserIdAndTypeAndTargetId(user.getId(), UserActionTypeEnum.FAVORITE_PLAYLIST.getCode(), playlistId);
        if (deleted > 0) {
            playlistMapper.updateFavoriteCount(playlistId, -1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordPlaySong(UserSongActionRequest request, HttpSession session) {
        User user = requireLoginUser(session);
        requireSong(request.getSongId());
        String actionType = UserActionTypeEnum.PLAY_SONG.getCode();
        UserAction action = userActionMapper.selectByUserIdAndTypeAndTargetId(user.getId(), actionType, request.getSongId());
        if (action == null) {
            insertAction(user.getId(), actionType, request.getSongId(), request.getProgressSeconds());
        } else {
            action.setActionTime(LocalDateTime.now());
            action.setProgressSeconds(normalizeProgress(request.getProgressSeconds()));
            userActionMapper.updateAction(action);
        }
        songMapper.updatePlayCount(request.getSongId(), 1);
    }

    private void validateUserUnique(String nickname, String email) {
        if (userMapper.selectByNickname(nickname.trim()) != null) {
            throw new BusinessException("nickname already exists");
        }
        if (StringUtils.hasText(email) && userMapper.selectByEmail(email.trim()) != null) {
            throw new BusinessException("email already exists");
        }
    }

    private void validateProfileUnique(String nickname, String email, Long currentUserId) {
        User nicknameUser = userMapper.selectByNickname(nickname.trim());
        if (nicknameUser != null && !nicknameUser.getId().equals(currentUserId)) {
            throw new BusinessException("nickname already exists");
        }
        if (StringUtils.hasText(email)) {
            User emailUser = userMapper.selectByEmail(email.trim());
            if (emailUser != null && !emailUser.getId().equals(currentUserId)) {
                throw new BusinessException("email already exists");
            }
        }
    }

    private void validateCaptcha(String captchaCode, HttpSession session) {
        String cachedCode = String.valueOf(session.getAttribute(SESSION_CAPTCHA_KEY));
        if (!StringUtils.hasText(cachedCode)) {
            throw new BusinessException("captcha is expired");
        }
        if (!cachedCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("captcha is invalid");
        }
        session.removeAttribute(SESSION_CAPTCHA_KEY);
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }

    private void insertAction(Long userId, String actionType, Long targetId, Integer progressSeconds) {
        UserAction entity = new UserAction();
        entity.setUserId(userId);
        entity.setActionType(actionType);
        entity.setTargetId(targetId);
        entity.setActionTime(LocalDateTime.now());
        entity.setProgressSeconds(normalizeProgress(progressSeconds));
        userActionMapper.insert(entity);
    }

    private Integer normalizeProgress(Integer progressSeconds) {
        return progressSeconds == null || progressSeconds < 0 ? 0 : progressSeconds;
    }

    private Long countAction(Long userId, UserActionTypeEnum actionType) {
        Long total = userActionMapper.countByUserIdAndType(userId, actionType.getCode());
        return total == null ? 0L : total;
    }

    private List<SongVO> loadSongsByAction(Long userId, UserActionTypeEnum actionType) {
        List<Long> targetIds = userActionMapper.selectTargetIdsByUserIdAndType(userId, actionType.getCode(), LIBRARY_LIMIT);
        if (targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Integer> orderMap = createOrderMap(targetIds);
        Map<String, String> categoryNameMap = buildCategoryNameMap();
        Map<Long, String> singerAvatarMap = new LinkedHashMap<>();
        return songMapper.selectByIds(targetIds)
            .stream()
            .sorted((left, right) -> Integer.compare(orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE), orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)))
            .map(item -> toSongVO(item, categoryNameMap, singerAvatarMap))
            .toList();
    }

    private List<PlaylistVO> loadPlaylistsByAction(Long userId, UserActionTypeEnum actionType) {
        List<Long> targetIds = userActionMapper.selectTargetIdsByUserIdAndType(userId, actionType.getCode(), LIBRARY_LIMIT);
        if (targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Integer> orderMap = createOrderMap(targetIds);
        Map<String, String> categoryNameMap = buildCategoryNameMap();
        return playlistMapper.selectByIds(targetIds)
            .stream()
            .sorted((left, right) -> Integer.compare(orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE), orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)))
            .map(item -> toPlaylistVO(item, categoryNameMap))
            .toList();
    }

    private Map<Long, Integer> createOrderMap(List<Long> ids) {
        Map<Long, Integer> orderMap = new LinkedHashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            orderMap.put(ids.get(index), index);
        }
        return orderMap;
    }

    private Map<String, String> buildCategoryNameMap() {
        List<DictItemVO> items = dictService.listEnabledItems(DictCodeEnum.CATEGORY);
        Map<String, String> categoryNameMap = new LinkedHashMap<>();
        for (DictItemVO item : items) {
            categoryNameMap.put(String.valueOf(item.getId()), item.getName());
        }
        return categoryNameMap;
    }

    private User requireEnabledUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"enabled".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException(401, "please login first");
        }
        return user;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        return user;
    }

    private User requireLoginUser(HttpSession session) {
        return requireEnabledUser(getCurrentUserId(session, true));
    }

    private Long getCurrentUserId(HttpSession session, boolean required) {
        Object userId = session.getAttribute(SESSION_USER_ID_KEY);
        if (userId instanceof Long value) {
            return value;
        }
        if (userId instanceof Integer value) {
            return value.longValue();
        }
        if (required) {
            throw new BusinessException(401, "please login first");
        }
        return null;
    }

    private Song requireSong(Long songId) {
        Song song = songMapper.selectById(songId);
        if (song == null || !"enabled".equalsIgnoreCase(song.getStatus())) {
            throw new BusinessException("song not found");
        }
        return song;
    }

    private Playlist requirePlaylist(Long playlistId) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null || !"enabled".equalsIgnoreCase(playlist.getStatus())) {
            throw new BusinessException("playlist not found");
        }
        return playlist;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException("status can not be blank");
        }
        String value = status.trim();
        if (!"enabled".equals(value) && !"disabled".equals(value)) {
            throw new BusinessException("unsupported status");
        }
        return value;
    }

    private UserVO toVO(User entity) {
        UserVO vo = new UserVO();
        vo.setId(entity.getId());
        vo.setNickname(entity.getNickname());
        vo.setAvatar(entity.getAvatar());
        vo.setGender(entity.getGender());
        vo.setEmail(entity.getEmail());
        vo.setSignature(entity.getSignature());
        vo.setStatus(entity.getStatus());
        vo.setLastLoginTime(entity.getLastLoginTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private SongVO toSongVO(Song entity, Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
        SongVO vo = new SongVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setSubtitle(entity.getSubtitle());
        vo.setSingerId(entity.getSingerId());
        vo.setSingerName(entity.getSingerName());
        vo.setSingerAvatar(resolveSingerAvatar(entity.getSingerId(), singerAvatarMap));
        vo.setCategory(entity.getCategory());
        vo.setCategoryName(resolveCategoryName(entity.getCategory(), categoryNameMap));
        vo.setTags(entity.getTags());
        vo.setCover(entity.getCover());
        vo.setAudioUrl(entity.getAudioUrl());
        vo.setDurationSeconds(entity.getDurationSeconds());
        vo.setLanguage(entity.getLanguage());
        vo.setIntro(entity.getIntro());
        vo.setReleaseDate(entity.getReleaseDate());
        vo.setStatus(entity.getStatus());
        vo.setRecommendFlag(entity.getRecommendFlag());
        vo.setPlayCount(entity.getPlayCount());
        vo.setLikeCount(entity.getLikeCount());
        vo.setFavoriteCount(entity.getFavoriteCount());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String resolveSingerAvatar(Long singerId, Map<Long, String> singerAvatarMap) {
        if (singerId == null) {
            return null;
        }
        if (singerAvatarMap.containsKey(singerId)) {
            return singerAvatarMap.get(singerId);
        }
        Singer singer = singerMapper.selectById(singerId);
        String avatar = singer == null ? null : singer.getAvatar();
        singerAvatarMap.put(singerId, avatar);
        return avatar;
    }

    private String resolveCategoryName(String category, Map<String, String> categoryNameMap) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        return categoryNameMap.getOrDefault(category, category);
    }

    private PlaylistVO toPlaylistVO(Playlist entity, Map<String, String> categoryNameMap) {
        PlaylistVO vo = new PlaylistVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setSubtitle(entity.getSubtitle());
        vo.setCover(entity.getCover());
        vo.setCategory(entity.getCategory());
        vo.setCategoryName(resolveCategoryName(entity.getCategory(), categoryNameMap));
        vo.setTags(entity.getTags());
        vo.setIntro(entity.getIntro());
        vo.setStatus(entity.getStatus());
        vo.setRecommendFlag(entity.getRecommendFlag());
        vo.setSongCount(entity.getSongCount());
        vo.setPlayCount(entity.getPlayCount());
        vo.setFavoriteCount(entity.getFavoriteCount());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
