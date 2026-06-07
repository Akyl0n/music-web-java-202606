package com.yinyu.service.impl;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.HomeRecommendQueryRequest;
import com.yinyu.entity.dto.HomeRecommendSaveRequest;
import com.yinyu.entity.dto.HomeRecommendSortRequest;
import com.yinyu.entity.dto.HomeRecommendStatusRequest;
import com.yinyu.entity.po.HomeRecommend;
import com.yinyu.entity.po.Playlist;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.vo.HomeRecommendTargetOptionVO;
import com.yinyu.entity.vo.HomeRecommendVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.HomeRecommendMapper;
import com.yinyu.mapper.PlaylistMapper;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.service.HomeRecommendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class HomeRecommendServiceImpl implements HomeRecommendService {

    private static final String POSITION_HOME_BANNER = "home_banner";
    private static final String POSITION_HOME_DAILY_SONG = "home_daily_song";
    private static final String TARGET_SONG = "song";
    private static final String TARGET_PLAYLIST = "playlist";
    private static final String TARGET_SINGER = "singer";

    private final HomeRecommendMapper homeRecommendMapper;
    private final SongMapper songMapper;
    private final PlaylistMapper playlistMapper;
    private final SingerMapper singerMapper;

    public HomeRecommendServiceImpl(
            HomeRecommendMapper homeRecommendMapper,
            SongMapper songMapper,
            PlaylistMapper playlistMapper,
            SingerMapper singerMapper
    ) {
        this.homeRecommendMapper = homeRecommendMapper;
        this.songMapper = songMapper;
        this.playlistMapper = playlistMapper;
        this.singerMapper = singerMapper;
    }

    @Override
    public ListData<HomeRecommendVO> listPage(HomeRecommendQueryRequest request) {
        HomeRecommendQueryRequest query = request == null ? new HomeRecommendQueryRequest() : request;
        List<HomeRecommendVO> list = homeRecommendMapper.selectPage(query).stream()
                .map(this::toVO)
                .toList();
        Long total = homeRecommendMapper.countPage(query);
        return new ListData<>(list, total == null ? 0L : total);
    }

    @Override
    public HomeRecommendVO detail(Long id) {
        return toVO(requireRecommend(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(HomeRecommendSaveRequest request) {
        validateRequest(request);
        validateDuplicate(request.getPositionCode(), request.getTargetType(), request.getTargetId(), null);
        HomeRecommend entity = new HomeRecommend();
        fillEntity(entity, request);
        entity.setSortNum(resolveSortNum(request.getSortNum(), request.getPositionCode()));
        homeRecommendMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(HomeRecommendSaveRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("id can not be null");
        }
        HomeRecommend existing = requireRecommend(request.getId());
        validateRequest(request);
        validateDuplicate(request.getPositionCode(), request.getTargetType(), request.getTargetId(), request.getId());
        fillEntity(existing, request);
        existing.setSortNum(request.getSortNum() != null ? request.getSortNum() : existing.getSortNum());
        homeRecommendMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireRecommend(id);
        homeRecommendMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(HomeRecommendStatusRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("ids can not be empty");
        }
        request.getIds().forEach(this::requireRecommend);
        homeRecommendMapper.updateStatus(request.getIds(), request.getStatus().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(HomeRecommendSortRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("sort ids can not be empty");
        }
        int size = request.getIds().size();
        for (int index = 0; index < size; index++) {
            Long id = request.getIds().get(index);
            requireRecommend(id);
            homeRecommendMapper.updateSort(id, (size - index) * 10);
        }
    }

    @Override
    public List<HomeRecommendVO> listEnabledByPosition(String positionCode) {
        if (!StringUtils.hasText(positionCode)) {
            throw new BusinessException("positionCode can not be blank");
        }
        return homeRecommendMapper.selectEnabledByPosition(positionCode.trim()).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<HomeRecommendTargetOptionVO> listTargetOptions(String targetType) {
        String normalizedTargetType = normalizeTargetType(targetType);
        return switch (normalizedTargetType) {
            case TARGET_SONG -> songMapper.selectEnabledHomeList(200).stream().map(this::toSongOption).toList();
            case TARGET_PLAYLIST -> playlistMapper.selectEnabledHomeList(200).stream().map(this::toPlaylistOption).toList();
            case TARGET_SINGER -> singerMapper.selectEnabledHomeList(200).stream().map(this::toSingerOption).toList();
            default -> throw new BusinessException("unsupported targetType");
        };
    }

    private void validateRequest(HomeRecommendSaveRequest request) {
        String positionCode = normalizePositionCode(request.getPositionCode());
        String targetType = normalizeTargetType(request.getTargetType());
        request.setPositionCode(positionCode);
        request.setTargetType(targetType);
        request.setCover(trimToNull(request.getCover()));
        request.setStatus(request.getStatus().trim());
        if (POSITION_HOME_BANNER.equals(positionCode) && !StringUtils.hasText(request.getCover())) {
            throw new BusinessException("cover can not be blank");
        }
        if (POSITION_HOME_DAILY_SONG.equals(positionCode)) {
            request.setCover(null);
        }
        validateTargetExists(targetType, request.getTargetId());
    }

    private void validateTargetExists(String targetType, Long targetId) {
        switch (targetType) {
            case TARGET_SONG -> {
                if (songMapper.selectById(targetId) == null) {
                    throw new BusinessException("song target not found");
                }
            }
            case TARGET_PLAYLIST -> {
                if (playlistMapper.selectById(targetId) == null) {
                    throw new BusinessException("playlist target not found");
                }
            }
            case TARGET_SINGER -> {
                if (singerMapper.selectById(targetId) == null) {
                    throw new BusinessException("singer target not found");
                }
            }
            default -> throw new BusinessException("unsupported targetType");
        }
    }

    private void validateDuplicate(String positionCode, String targetType, Long targetId, Long excludeId) {
        Integer count = homeRecommendMapper.countByPositionAndTarget(positionCode, targetType, targetId, excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("same target already exists in current position");
        }
    }

    private HomeRecommend requireRecommend(Long id) {
        HomeRecommend entity = homeRecommendMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("home recommend not found");
        }
        return entity;
    }

    private void fillEntity(HomeRecommend entity, HomeRecommendSaveRequest request) {
        entity.setPositionCode(request.getPositionCode());
        entity.setTargetType(request.getTargetType());
        entity.setTargetId(request.getTargetId());
        entity.setCover(request.getCover());
        entity.setStatus(request.getStatus());
    }

    private Integer resolveSortNum(Integer sortNum, String positionCode) {
        if (sortNum != null) {
            return sortNum;
        }
        Integer maxSortNum = homeRecommendMapper.selectMaxSortNum(positionCode);
        return (maxSortNum == null ? 0 : maxSortNum) + 10;
    }

    private String normalizePositionCode(String positionCode) {
        if (!StringUtils.hasText(positionCode)) {
            throw new BusinessException("positionCode can not be blank");
        }
        String value = positionCode.trim();
        if (!POSITION_HOME_BANNER.equals(value) && !POSITION_HOME_DAILY_SONG.equals(value)) {
            throw new BusinessException("unsupported positionCode");
        }
        return value;
    }

    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException("targetType can not be blank");
        }
        String value = targetType.trim();
        if (!TARGET_SONG.equals(value) && !TARGET_PLAYLIST.equals(value) && !TARGET_SINGER.equals(value)) {
            throw new BusinessException("unsupported targetType");
        }
        return value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private HomeRecommendVO toVO(HomeRecommend entity) {
        HomeRecommendVO vo = new HomeRecommendVO();
        vo.setId(entity.getId());
        vo.setPositionCode(entity.getPositionCode());
        vo.setPositionLabel(resolvePositionLabel(entity.getPositionCode()));
        vo.setTargetType(entity.getTargetType());
        vo.setTargetTypeLabel(resolveTargetTypeLabel(entity.getTargetType()));
        vo.setTargetId(entity.getTargetId());
        vo.setCover(entity.getCover());
        vo.setSortNum(entity.getSortNum());
        vo.setStatus(entity.getStatus());
        fillTargetInfo(vo);
        return vo;
    }

    private void fillTargetInfo(HomeRecommendVO vo) {
        switch (vo.getTargetType()) {
            case TARGET_SONG -> {
                Song song = songMapper.selectById(vo.getTargetId());
                if (song != null) {
                    vo.setTargetName(song.getName());
                    vo.setTargetCover(song.getCover());
                }
            }
            case TARGET_PLAYLIST -> {
                Playlist playlist = playlistMapper.selectById(vo.getTargetId());
                if (playlist != null) {
                    vo.setTargetName(playlist.getName());
                    vo.setTargetCover(playlist.getCover());
                }
            }
            case TARGET_SINGER -> {
                Singer singer = singerMapper.selectById(vo.getTargetId());
                if (singer != null) {
                    vo.setTargetName(singer.getName());
                    vo.setTargetCover(singer.getAvatar());
                }
            }
            default -> {
            }
        }
    }

    private String resolvePositionLabel(String positionCode) {
        return switch (positionCode) {
            case POSITION_HOME_BANNER -> "首页轮播";
            case POSITION_HOME_DAILY_SONG -> "今日推荐";
            default -> positionCode;
        };
    }

    private String resolveTargetTypeLabel(String targetType) {
        return switch (targetType) {
            case TARGET_SONG -> "歌曲";
            case TARGET_PLAYLIST -> "歌单";
            case TARGET_SINGER -> "歌手";
            default -> targetType;
        };
    }

    private HomeRecommendTargetOptionVO toSongOption(Song song) {
        HomeRecommendTargetOptionVO vo = new HomeRecommendTargetOptionVO();
        vo.setId(song.getId());
        vo.setName(song.getName());
        vo.setCover(song.getCover());
        return vo;
    }

    private HomeRecommendTargetOptionVO toPlaylistOption(Playlist playlist) {
        HomeRecommendTargetOptionVO vo = new HomeRecommendTargetOptionVO();
        vo.setId(playlist.getId());
        vo.setName(playlist.getName());
        vo.setCover(playlist.getCover());
        return vo;
    }

    private HomeRecommendTargetOptionVO toSingerOption(Singer singer) {
        HomeRecommendTargetOptionVO vo = new HomeRecommendTargetOptionVO();
        vo.setId(singer.getId());
        vo.setName(singer.getName());
        vo.setCover(singer.getAvatar());
        return vo;
    }
}
