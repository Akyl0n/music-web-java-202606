package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardVO {

    private List<AdminDashboardMetricVO> metrics;

    private List<AdminDashboardStatVO> contentStats;

    private List<AdminDashboardStatVO> operationStats;

    private List<AdminDashboardNoticeVO> quickEntries;
}
