package com.yinyu.admin.service;

import com.yinyu.entity.dto.AdminLoginRequest;
import com.yinyu.entity.vo.AdminSessionVO;
import com.yinyu.entity.vo.UserCaptchaVO;
import jakarta.servlet.http.HttpSession;

public interface AdminAuthService {

    UserCaptchaVO generateCaptcha(HttpSession session);

    AdminSessionVO login(AdminLoginRequest request, HttpSession session);

    AdminSessionVO current(HttpSession session);

    void logout(HttpSession session);
}
