package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class SingerDetailVO {

    private SingerVO singer;

    private List<SongVO> songs;

    private Long totalSongs;
}
