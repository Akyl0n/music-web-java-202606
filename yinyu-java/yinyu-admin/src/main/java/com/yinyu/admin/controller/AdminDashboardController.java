package com.yinyu.admin.controller;

import com.yinyu.admin.service.AdminDashboardService;
import com.yinyu.api.ApiResponse;
import com.yinyu.entity.vo.AdminDashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public ApiResponse<AdminDashboardVO> detail() {
        return ApiResponse.success(adminDashboardService.getDashboard());
    }
}
