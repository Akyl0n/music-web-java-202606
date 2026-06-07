package com.yinyu.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeRecommendSortRequest {

    private List<Long> ids;
}
