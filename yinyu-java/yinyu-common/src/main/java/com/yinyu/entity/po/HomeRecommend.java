package com.yinyu.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeRecommend {

    private Long id;

    private String positionCode;

    private String targetType;

    private Long targetId;

    private String cover;

    private Integer sortNum;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
