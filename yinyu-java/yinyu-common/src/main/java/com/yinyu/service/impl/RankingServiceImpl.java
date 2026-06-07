package com.yinyu.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingServiceImpl implements RankingService {

    private static final int MAX_RANK_SIZE = 30;

    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final DictService dictService;

    public RankingServiceImpl(SongMapper songMapper, SingerMapper singerMapper, DictService dictService) {
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.dictService = dictService;
    }

    @Override
    public List<RankingBoardVO> listBoards() {
        List<Song> songs = songMapper.selectEnabledRankingList();
        return List.of(
            buildBoard("hot", "实时热播", "根据站内当前播放热度、收藏热度与推荐状态综合排序，适合快速发现当下最热门的作品。", songs),
            buildBoard("new", "新歌榜", "聚焦近期上新的古风作品，优先展示近阶段发布且热度持续上升的歌曲。", songs),
            buildBoard("favorite", "收藏榜", "按喜欢与收藏热度排序，更偏长期耐听和反复回访的内容。", songs)
        );
    }

    @Override
    public RankingDetailVO getDetail(String boardCode) {
        String normalizedCode = StringUtils.hasText(boardCode) ? boardCode.trim().toLowerCase() : "hot";
        List<Song> songs = songMapper.selectEnabledRankingList();
        RankingBoardVO currentBoard = listBoards().stream()
            .filter(item -> item.getCode().equals(normalizedCode))
            .findFirst()
            .orElseThrow(() -> new BusinessException("ranking board not found"));

        Map<String, String> categoryNameMap = buildCategoryNameMap();
        Map<Long, String> singerAvatarMap = new LinkedHashMap<>();
        List<SongVO> rankingSongs = sortSongs(normalizedCode, songs).stream()
            .limit(MAX_RANK_SIZE)
            .map(item -> toSongVO(item, categoryNameMap, singerAvatarMap))
            .toList();

        RankingDetailVO vo = new RankingDetailVO();
        vo.setBoards(listBoards());
        vo.setCurrentBoard(currentBoard);
        vo.setSongs(rankingSongs);
        return vo;
    }

    private RankingBoardVO buildBoard(String code, String name, String note, List<Song> songs) {
        RankingBoardVO vo = new RankingBoardVO();
        vo.setCode(code);
        vo.setName(name);
        vo.setNote(note);
        vo.setTotalSongs(Math.min(sortSongs(code, songs).size(), MAX_RANK_SIZE));
        vo.setUpdateText(resolveUpdateText(songs));
        return vo;
    }

    private List<Song> sortSongs(String boardCode, List<Song> songs) {
        Comparator<Song> comparator;
        switch (boardCode) {
            case "new" -> comparator = Comparator
                .comparing(Song::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getRecommendFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getPlayCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            case "favorite" -> comparator = Comparator
                .comparing(Song::getFavoriteCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getPlayCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getRecommendFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            case "hot" -> comparator = Comparator
                .comparing(Song::getPlayCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getFavoriteCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getRecommendFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Song::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            default -> throw new BusinessException("ranking board not found");
        }
        return songs.stream().sorted(comparator).toList();
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
