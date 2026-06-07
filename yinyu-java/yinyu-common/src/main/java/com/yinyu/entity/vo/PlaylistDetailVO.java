package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistDetailVO {

    private PlaylistVO playlist;

    private List<SongVO> songs;
}
