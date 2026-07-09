package com.yinyu.service.ranking;

import com.yinyu.entity.po.Song;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Order(10)
public class HotRankingStrategy implements RankingStrategy {

    @Override
    public String code() {
        return "hot";
    }

    @Override
    public String name() {
        return "实时热播";
    }

    @Override
    public String note() {
        return "根据站内当前播放热度、收藏热度与推荐状态综合排序，适合快速发现当下最热门的作品。";
    }

    @Override
    public List<Song> sort(List<Song> songs) {
        return songs.stream()
                .sorted(Comparator
                        .comparing(Song::getPlayCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getFavoriteCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getRecommendFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
