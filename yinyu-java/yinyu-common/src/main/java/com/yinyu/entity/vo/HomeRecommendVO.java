package com.yinyu.entity.vo;

import lombok.Data;

@Data
public class HomeRecommendVO {

    private Long id;

    private String positionCode;

    private String positionLabel;

    private String targetType;

    private String targetTypeLabel;

    private Long targetId;

    private String targetName;

    private String targetCover;

    private String cover;

    private Integer sortNum;

    private String status;
}
