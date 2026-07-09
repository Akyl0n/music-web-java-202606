package com.yinyu.service.impl;

import com.yinyu.api.ListData;
import com.yinyu.config.RedisCacheConfig;
import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.dto.PlaylistSaveRequest;
import com.yinyu.entity.dto.PlaylistStatusRequest;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.po.Playlist;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.PlaylistDetailVO;
import com.yinyu.entity.vo.PlaylistVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.PlaylistMapper;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.service.DictService;
import com.yinyu.search.SearchIndexService;
import com.yinyu.search.SearchPage;
import com.yinyu.service.InfrastructureEventPublisher;
import com.yinyu.service.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistMapper playlistMapper;
    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final DictService dictService;
    private final SearchIndexService searchIndexService;
    private final InfrastructureEventPublisher eventPublisher;

    public PlaylistServiceImpl(PlaylistMapper playlistMapper, SongMapper songMapper, SingerMapper singerMapper, DictService dictService, SearchIndexService searchIndexService, InfrastructureEventPublisher eventPublisher) {
        this.playlistMapper = playlistMapper;
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.dictService = dictService;
        this.searchIndexService = searchIndexService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ListData<PlaylistVO> listPage(PlaylistQueryRequest request) {
        PlaylistQueryRequest query = request == null ? new PlaylistQueryRequest() : request;
        Map<String, String> categoryNameMap = buildCategoryNameMap();
        SearchPage searchPage = searchIndexService.searchPlaylists(query);
        if (searchPage != null) {
            Map<Long, Integer> orderMap = createOrderMap(searchPage.ids());
            List<PlaylistVO> searchList = searchPage.ids().isEmpty() ? List.of() : playlistMapper.selectByIds(searchPage.ids())
                    .stream()
                    .sorted((left, right) -> Integer.compare(orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE), orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)))
                    .map(entity -> toVO(entity, false, categoryNameMap))
                    .toList();
            return new ListData<>(searchList, searchPage.total());
        }
        List<PlaylistVO> list = playlistMapper.selectPage(query)
            .stream()
            .map(entity -> toVO(entity, false, categoryNameMap))
            .toList();
        Long total = playlistMapper.countPage(query);
        return new ListData<>(list, total == null ? 0L : total);
    }

    @Override
    public PlaylistVO detail(Long id) {
        Playlist entity = requirePlaylist(id);
        return toVO(entity, true, buildCategoryNameMap());
    }

    @Override
    public PlaylistDetailVO detailWithSongs(Long id) {
        Playlist entity = requirePlaylist(id);
        Map<String, String> categoryNameMap = buildCategoryNameMap();
        PlaylistVO playlist = toVO(entity, true, categoryNameMap);
        List<Long> songIds = playlist.getSongIds();
        List<SongVO> songs = List.of();
        if (!CollectionUtils.isEmpty(songIds)) {
            Map<Long, Integer> orderMap = createOrderMap(songIds);
            Map<Long, String> singerAvatarMap = new LinkedHashMap<>();
            songs = songMapper.selectByIds(songIds).stream()
                .sorted((left, right) -> Integer.compare(orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE), orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)))
                .map(item -> toSongVO(item, categoryNameMap, singerAvatarMap))
                .toList();
        }
        PlaylistDetailVO vo = new PlaylistDetailVO();
        vo.setPlaylist(playlist);
        vo.setSongs(songs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(PlaylistSaveRequest request) {
        validateNameUnique(request.getName(), null);
        List<Long> songIds = normalizeSongIds(request.getSongIds());
        validateSongsExist(songIds);
        Playlist entity = new Playlist();
        fillPlaylist(entity, request, songIds);
        playlistMapper.insert(entity);
        savePlaylistSongs(entity.getId(), songIds);
        publishPlaylistChanged(entity.getId(), "upsert");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PlaylistSaveRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("id can not be null");
        }
        Playlist existing = requirePlaylist(request.getId());
        validateNameUnique(request.getName(), request.getId());
        List<Long> songIds = normalizeSongIds(request.getSongIds());
        validateSongsExist(songIds);
        fillPlaylist(existing, request, songIds);
        playlistMapper.update(existing);
        savePlaylistSongs(existing.getId(), songIds);
        publishPlaylistChanged(existing.getId(), "upsert");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requirePlaylist(id);
        playlistMapper.deletePlaylistSongsByPlaylistId(id);
        playlistMapper.deleteById(id);
        publishPlaylistChanged(id, "delete");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(PlaylistStatusRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("ids can not be empty");
        }
        request.getIds().forEach(this::requirePlaylist);
        playlistMapper.updateStatus(request.getIds(), request.getStatus().trim());
        request.getIds().forEach(id -> publishPlaylistChanged(id, "upsert"));
    }

    private void publishPlaylistChanged(Long id, String action) {
        eventPublisher.publishContentChanged(SearchIndexService.TYPE_PLAYLIST, id, action);
        eventPublisher.publishCacheInvalidation(List.of(RedisCacheConfig.CACHE_HOME_PAGE));
    }

    private void fillPlaylist(Playlist entity, PlaylistSaveRequest request, List<Long> songIds) {
        entity.setName(request.getName().trim());
        entity.setSubtitle(trimToNull(request.getSubtitle()));
        entity.setCover(trimToNull(request.getCover()));
        entity.setCategory(trimToNull(request.getCategory()));
        entity.setTags(normalizeTags(request.getTags()));
        entity.setIntro(trimToNull(request.getIntro()));
        entity.setStatus(request.getStatus().trim());
        entity.setRecommendFlag(normalizeRecommendFlag(request.getRecommendFlag()));
        entity.setSongCount(songIds.size());
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private Integer normalizeRecommendFlag(Integer recommendFlag) {
        return recommendFlag != null && recommendFlag == 1 ? 1 : 0;
    }

    private void validateNameUnique(String name, Long excludeId) {
        Integer count = playlistMapper.countByName(name.trim(), excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("playlist name already exists");
        }
    }

    private Playlist requirePlaylist(Long id) {
        Playlist entity = playlistMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("playlist not found");
        }
        return entity;
    }

    private List<Long> normalizeSongIds(List<Long> songIds) {
        if (CollectionUtils.isEmpty(songIds)) {
            return List.of();
        }
        return songIds.stream().filter(id -> id != null).distinct().toList();
    }

    private void validateSongsExist(List<Long> songIds) {
        for (Long songId : songIds) {
            Song song = songMapper.selectById(songId);
            if (song == null) {
                throw new BusinessException("song not found: " + songId);
            }
        }
    }

    private void savePlaylistSongs(Long playlistId, List<Long> songIds) {
        playlistMapper.deletePlaylistSongsByPlaylistId(playlistId);
        if (!CollectionUtils.isEmpty(songIds)) {
            playlistMapper.batchInsertPlaylistSongs(playlistId, songIds);
        }
    }

    private String normalizeTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return null;
        }
        return String.join(",",
            List.of(tags.split(","))
                .stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList()
        );
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Map<Long, Integer> createOrderMap(List<Long> ids) {
        return ids.stream().collect(java.util.stream.Collectors.toMap(Function.identity(), ids::indexOf, (left, right) -> left));
    }

    private Map<String, String> buildCategoryNameMap() {
        List<DictItemVO> items = dictService.listEnabledItems(DictCodeEnum.CATEGORY);
        Map<String, String> categoryNameMap = new LinkedHashMap<>();
        for (DictItemVO item : items) {
            categoryNameMap.put(String.valueOf(item.getId()), item.getName());
        }
        return categoryNameMap;
    }

    private PlaylistVO toVO(Playlist entity, boolean includeSongIds, Map<String, String> categoryNameMap) {
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
        if (includeSongIds) {
            vo.setSongIds(playlistMapper.selectSongIdsByPlaylistId(entity.getId()));
        }
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
}
