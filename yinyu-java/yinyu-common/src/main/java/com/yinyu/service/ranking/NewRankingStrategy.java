package com.yinyu.service.ranking;

import com.yinyu.entity.po.Song;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Order(20)
public class NewRankingStrategy implements RankingStrategy {

    @Override
    public String code() {
        return "new";
    }

    @Override
    public String name() {
        return "新歌榜";
    }

    @Override
    public String note() {
        return "聚焦近期上新的古风作品，优先展示近阶段发布且热度持续上升的歌曲。";
    }

    @Override
    public List<Song> sort(List<Song> songs) {
        return songs.stream()
                .sorted(Comparator
                        .comparing(Song::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getRecommendFlag, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getPlayCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Song::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
