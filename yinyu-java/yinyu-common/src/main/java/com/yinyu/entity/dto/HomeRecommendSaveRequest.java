package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeRecommendSaveRequest {

    private Long id;

    @NotBlank(message = "positionCode can not be blank")
    private String positionCode;

    @NotBlank(message = "targetType can not be blank")
    private String targetType;

    @NotNull(message = "targetId can not be null")
    private Long targetId;

    private String cover;

    private Integer sortNum;

    @NotBlank(message = "status can not be blank")
    private String status;
}
