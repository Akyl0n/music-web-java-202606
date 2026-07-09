package com.yinyu.service.impl;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.dto.SongSaveRequest;
import com.yinyu.entity.dto.SongStatusRequest;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.search.SearchIndexService;
import com.yinyu.search.SearchPage;
import com.yinyu.service.InfrastructureEventPublisher;
import com.yinyu.service.DictService;
import com.yinyu.service.SongService;
import com.yinyu.config.RedisCacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SongServiceImpl implements SongService {

    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final DictService dictService;
    private final SearchIndexService searchIndexService;
    private final InfrastructureEventPublisher eventPublisher;

    public SongServiceImpl(SongMapper songMapper, SingerMapper singerMapper, DictService dictService, SearchIndexService searchIndexService, InfrastructureEventPublisher eventPublisher) {
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.dictService = dictService;
        this.searchIndexService = searchIndexService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ListData<SongVO> listPage(SongQueryRequest request) {
        SongQueryRequest query = request == null ? new SongQueryRequest() : request;
        Map<String, String> categoryNameMap = buildCategoryNameMap();
        Map<Long, String> singerAvatarMap = new LinkedHashMap<>();
        SearchPage searchPage = searchIndexService.searchSongs(query);
        if (searchPage != null) {
            Map<Long, Integer> orderMap = createOrderMap(searchPage.ids());
            List<SongVO> searchList = searchPage.ids().isEmpty() ? List.of() : songMapper.selectByIds(searchPage.ids())
                    .stream()
                    .sorted((left, right) -> Integer.compare(orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE), orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)))
                    .map(item -> toVO(item, categoryNameMap, singerAvatarMap))
                    .toList();
            return new ListData<>(searchList, searchPage.total());
        }
        List<SongVO> list = songMapper.selectPage(query)
            .stream()
            .map(item -> toVO(item, categoryNameMap, singerAvatarMap))
            .toList();
        Long total = songMapper.countPage(query);
        return new ListData<>(list, total == null ? 0L : total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SongSaveRequest request) {
        Singer singer = requireSinger(request.getSingerId());
        validateNameUnique(request.getName(), request.getSingerId(), null);
        Song entity = new Song();
        fillSong(entity, request, singer);
        songMapper.insert(entity);
        publishSongChanged(entity.getId(), "upsert");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SongSaveRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("id can not be null");
        }
        Song existing = requireSong(request.getId());
        Singer singer = requireSinger(request.getSingerId());
        validateNameUnique(request.getName(), request.getSingerId(), request.getId());
        fillSong(existing, request, singer);
        songMapper.update(existing);
        publishSongChanged(existing.getId(), "upsert");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireSong(id);
        songMapper.deleteById(id);
        publishSongChanged(id, "delete");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(SongStatusRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException("ids can not be empty");
        }
        request.getIds().forEach(this::requireSong);
        songMapper.updateStatus(request.getIds(), request.getStatus().trim());
        request.getIds().forEach(id -> publishSongChanged(id, "upsert"));
    }

    private void publishSongChanged(Long id, String action) {
        eventPublisher.publishContentChanged(SearchIndexService.TYPE_SONG, id, action);
        eventPublisher.publishCacheInvalidation(List.of(RedisCacheConfig.CACHE_HOME_PAGE, RedisCacheConfig.CACHE_RANKING_BOARDS, RedisCacheConfig.CACHE_RANKING_DETAIL));
    }

    private Map<Long, Integer> createOrderMap(List<Long> ids) {
        Map<Long, Integer> orderMap = new LinkedHashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            orderMap.put(ids.get(index), index);
        }
        return orderMap;
    }

    private void fillSong(Song entity, SongSaveRequest request, Singer singer) {
        entity.setName(request.getName().trim());
        entity.setSubtitle(trimToNull(request.getSubtitle()));
        entity.setSingerId(singer.getId());
        entity.setSingerName(singer.getName());
        entity.setCategory(trimToNull(request.getCategory()));
        entity.setTags(normalizeTags(request.getTags()));
        entity.setCover(trimToNull(request.getCover()));
        entity.setAudioUrl(trimToNull(request.getAudioUrl()));
        entity.setDurationSeconds(request.getDurationSeconds());
        entity.setLanguage(trimToNull(request.getLanguage()));
        entity.setIntro(trimToNull(request.getIntro()));
        entity.setReleaseDate(request.getReleaseDate());
        entity.setStatus(request.getStatus().trim());
        entity.setRecommendFlag(normalizeRecommendFlag(request.getRecommendFlag()));
        entity.setRemark(trimToNull(request.getRemark()));
    }

    private Integer normalizeRecommendFlag(Integer recommendFlag) {
        return recommendFlag != null && recommendFlag == 1 ? 1 : 0;
    }

    private void validateNameUnique(String name, Long singerId, Long excludeId) {
        Integer count = songMapper.countByNameAndSingerId(name.trim(), singerId, excludeId);
        if (count != null && count > 0) {
            throw new BusinessException("song name already exists for singer");
        }
    }

    private Singer requireSinger(Long singerId) {
        Singer singer = singerMapper.selectById(singerId);
        if (singer == null) {
            throw new BusinessException("singer not found");
        }
        return singer;
    }

    private Song requireSong(Long id) {
        Song entity = songMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("song not found");
        }
        return entity;
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

    private Map<String, String> buildCategoryNameMap() {
        List<DictItemVO> items = dictService.listEnabledItems(DictCodeEnum.CATEGORY);
        Map<String, String> categoryNameMap = new LinkedHashMap<>();
        for (DictItemVO item : items) {
            categoryNameMap.put(String.valueOf(item.getId()), item.getName());
        }
        return categoryNameMap;
    }

    private SongVO toVO(Song entity, Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
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
