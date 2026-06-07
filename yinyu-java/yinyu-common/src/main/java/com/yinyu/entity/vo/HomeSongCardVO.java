package com.yinyu.entity.vo;

import lombok.Data;

@Data
public class HomeSongCardVO {

    private Long id;

    private String targetType;

    private Long targetId;

    private String no;

    private String name;

    private String desc;

    private String meta;

    private String time;

    private String singer;

    private Long playlistId;

    private String cover;

    private String singerAvatar;

    private String audioUrl;

    private Integer durationSeconds;
}
