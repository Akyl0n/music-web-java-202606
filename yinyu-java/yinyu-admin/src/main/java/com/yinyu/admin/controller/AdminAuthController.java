package com.yinyu.admin.controller;

import com.yinyu.admin.service.AdminAuthService;
import com.yinyu.api.ApiResponse;
import com.yinyu.entity.dto.AdminLoginRequest;
import com.yinyu.entity.vo.AdminSessionVO;
import com.yinyu.entity.vo.UserCaptchaVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/captcha")
    public ApiResponse<UserCaptchaVO> captcha(HttpSession session) {
        return ApiResponse.success(adminAuthService.generateCaptcha(session));
    }

    @PostMapping("/login")
    public ApiResponse<AdminSessionVO> login(@RequestBody @Valid AdminLoginRequest request, HttpSession session) {
        return ApiResponse.success(adminAuthService.login(request, session));
    }

    @GetMapping("/current")
    public ApiResponse<AdminSessionVO> current(HttpSession session) {
        return ApiResponse.success(adminAuthService.current(session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        adminAuthService.logout(session);
        return ApiResponse.success("logout success", null);
    }
}
