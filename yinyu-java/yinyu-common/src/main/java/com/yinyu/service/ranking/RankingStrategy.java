package com.yinyu.service.ranking;

import com.yinyu.entity.po.Song;

import java.util.List;

public interface RankingStrategy {

    String code();

    String name();

    String note();

    List<Song> sort(List<Song> songs);
}
