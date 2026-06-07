package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class HomePlaylistCardVO {

    private Long id;

    private String targetType;

    private Long targetId;

    private String tag;

    private String title;

    private String desc;

    private String meta;

    private String cover;

    private String category;

    private String categoryName;

    private List<Long> songIds;

    private Long firstSongId;
}
