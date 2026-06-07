package com.yinyu.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Playlist {

    private Long id;

    private String name;

    private String subtitle;

    private String cover;

    private String category;

    private String tags;

    private String intro;

    private String status;

    private Integer recommendFlag;

    private Integer songCount;

    private Long playCount;

    private Long favoriteCount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
