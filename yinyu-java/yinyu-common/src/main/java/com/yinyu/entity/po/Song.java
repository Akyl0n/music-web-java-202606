package com.yinyu.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Song {

    private Long id;

    private String name;

    private String subtitle;

    private Long singerId;

    private String singerName;

    private String category;

    private String tags;

    private String cover;

    private String audioUrl;

    private Integer durationSeconds;

    private String language;

    private String intro;

    private LocalDate releaseDate;

    private String status;

    private Integer recommendFlag;

    private Long playCount;

    private Long likeCount;

    private Long favoriteCount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
