package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class HomeRecommendStatusRequest {

    private List<Long> ids;

    @NotBlank(message = "status can not be blank")
    private String status;
}
