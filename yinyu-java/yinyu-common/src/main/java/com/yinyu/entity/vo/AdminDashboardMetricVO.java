package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardMetricVO {

    private String title;

    private String primaryLabel;

    private String primaryValue;

    private String secondaryLabel;

    private String secondaryValue;

    private String accent;

    private List<Integer> trend;
}
