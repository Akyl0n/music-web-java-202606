package com.yinyu.service.impl;

import com.yinyu.config.RedisCacheConfig;
import com.yinyu.entity.enums.DictCodeEnum;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.RankingBoardVO;
import com.yinyu.entity.vo.RankingDetailVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.service.DictService;
import com.yinyu.service.RankingService;
import com.yinyu.service.ranking.RankingStrategy;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    private static final int MAX_RANK_SIZE = 30;

    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final DictService dictService;
    private final List<RankingStrategy> rankingStrategies;
    private final Map<String, RankingStrategy> rankingStrategyMap;

    public RankingServiceImpl(SongMapper songMapper, SingerMapper singerMapper, DictService dictService, List<RankingStrategy> rankingStrategies) {
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.dictService = dictService;
        this.rankingStrategies = rankingStrategies;
        this.rankingStrategyMap = rankingStrategies.stream()
                .collect(Collectors.toUnmodifiableMap(RankingStrategy::code, Function.identity()));
    }

    @Override
    @Cacheable(cacheNames = RedisCacheConfig.CACHE_RANKING_BOARDS, key = "'all'")
    public List<RankingBoardVO> listBoards() {
        List<Song> songs = songMapper.selectEnabledRankingList();
        return rankingStrategies.stream()
            .map(strategy -> buildBoard(strategy, songs))
            .toList();
    }

    @Override
    @Cacheable(cacheNames = RedisCacheConfig.CACHE_RANKING_DETAIL, key = "#boardCode == null ? 'hot' : #boardCode")
    public RankingDetailVO getDetail(String boardCode) {
        String normalizedCode = StringUtils.hasText(boardCode) ? boardCode.trim().toLowerCase() : "hot";
        RankingStrategy strategy = requireStrategy(normalizedCode);
        List<Song> songs = songMapper.selectEnabledRankingList();
        RankingBoardVO currentBoard = listBoards().stream()
            .filter(item -> item.getCode().equals(normalizedCode))
            .findFirst()
            .orElseThrow(() -> new BusinessException("ranking board not found"));

        Map<String, String> categoryNameMap = buildCategoryNameMap();
        Map<Long, String> singerAvatarMap = new LinkedHashMap<>();
        List<SongVO> rankingSongs = strategy.sort(songs).stream()
            .limit(MAX_RANK_SIZE)
            .map(item -> toSongVO(item, categoryNameMap, singerAvatarMap))
            .toList();

        RankingDetailVO vo = new RankingDetailVO();
        vo.setBoards(listBoards());
        vo.setCurrentBoard(currentBoard);
        vo.setSongs(rankingSongs);
        return vo;
    }

    private RankingBoardVO buildBoard(RankingStrategy strategy, List<Song> songs) {
        RankingBoardVO vo = new RankingBoardVO();
        vo.setCode(strategy.code());
        vo.setName(strategy.name());
        vo.setNote(strategy.note());
        vo.setTotalSongs(Math.min(strategy.sort(songs).size(), MAX_RANK_SIZE));
        vo.setUpdateText(resolveUpdateText(songs));
        return vo;
    }

    private RankingStrategy requireStrategy(String boardCode) {
        RankingStrategy strategy = rankingStrategyMap.get(boardCode);
        if (strategy == null) {
            throw new BusinessException("ranking board not found");
        }
        return strategy;
    }

    private Map<String, String> buildCategoryNameMap() {
        List<DictItemVO> items = dictService.listEnabledItems(DictCodeEnum.CATEGORY);
        Map<String, String> categoryNameMap = new LinkedHashMap<>();
        for (DictItemVO item : items) {
            if (item.getId() != null) {
                categoryNameMap.put(String.valueOf(item.getId()), item.getName());
            }
        }
        return categoryNameMap;
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

    private String resolveUpdateText(List<Song> songs) {
        LocalDateTime latestUpdateTime = songs.stream()
            .map(Song::getUpdateTime)
            .filter(item -> item != null)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        if (latestUpdateTime == null) {
            return "最近更新";
        }
        LocalDate date = latestUpdateTime.toLocalDate();
        return date + " 更新";
    }
}
