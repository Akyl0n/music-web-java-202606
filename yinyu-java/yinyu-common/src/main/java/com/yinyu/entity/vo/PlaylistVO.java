package com.yinyu.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaylistVO {

    private Long id;

    private String name;

    private String subtitle;

    private String cover;

    private String category;

    private String categoryName;

    private String tags;

    private String intro;

    private String status;

    private Integer recommendFlag;

    private Integer songCount;

    private Long playCount;

    private Long favoriteCount;

    private String remark;

    private List<Long> songIds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
