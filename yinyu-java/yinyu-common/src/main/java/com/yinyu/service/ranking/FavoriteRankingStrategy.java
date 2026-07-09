package com.yinyu.service.ranking;

import com.yinyu.entity.po.Song;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Order(30)
public class FavoriteRankingStrategy implements RankingStrategy {

    @Override
    public String code() {
        return "favorite";
    }

    @Override
    public String name() {
        return "收藏榜";
    }

    @Override
    public String note() {
        return "按喜欢与收藏热度排序，更偏长期耐听和反复回访的内容。";
    }

    @Override
    public List<Song> sort(List<Song> songs) {
        return songs.stream()
                .sorted(Comparator
                        .comparing(Song::getFavoriteCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getPlayCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getRecommendFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
