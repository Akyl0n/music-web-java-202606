package com.yinyu.admin.service.impl;

import com.wf.captcha.SpecCaptcha;
import com.yinyu.admin.config.AdminAuthProperties;
import com.yinyu.admin.interceptor.AdminSessionInterceptor;
import com.yinyu.admin.service.AdminAuthService;
import com.yinyu.entity.dto.AdminLoginRequest;
import com.yinyu.entity.vo.AdminSessionVO;
import com.yinyu.entity.vo.UserCaptchaVO;
import com.yinyu.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final String SESSION_ADMIN_CAPTCHA = "admin:auth:captcha";

    private final AdminAuthProperties adminAuthProperties;

    public AdminAuthServiceImpl(AdminAuthProperties adminAuthProperties) {
        this.adminAuthProperties = adminAuthProperties;
    }

    @Override
    public UserCaptchaVO generateCaptcha(HttpSession session) {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text().toLowerCase();
        session.setAttribute(SESSION_ADMIN_CAPTCHA, code);
        UserCaptchaVO vo = new UserCaptchaVO();
        vo.setImageBase64(captcha.toBase64());
        return vo;
    }

    @Override
    public AdminSessionVO login(AdminLoginRequest request, HttpSession session) {
        validateCaptcha(request.getCaptchaCode(), session);
        if (!adminAuthProperties.getUsername().equals(request.getUsername().trim())
            || !adminAuthProperties.getPassword().equals(request.getPassword().trim())) {
            throw new BusinessException(401, "username or password is invalid");
        }
        session.setAttribute(AdminSessionInterceptor.SESSION_ADMIN_USERNAME, adminAuthProperties.getUsername());
        return buildSessionVO();
    }

    @Override
    public AdminSessionVO current(HttpSession session) {
        Object username = session.getAttribute(AdminSessionInterceptor.SESSION_ADMIN_USERNAME);
        if (username == null) {
            throw new BusinessException(401, "please login first");
        }
        return buildSessionVO();
    }

    @Override
    public void logout(HttpSession session) {
        session.removeAttribute(AdminSessionInterceptor.SESSION_ADMIN_USERNAME);
    }

    private void validateCaptcha(String captchaCode, HttpSession session) {
        String cachedCode = String.valueOf(session.getAttribute(SESSION_ADMIN_CAPTCHA));
        if (!StringUtils.hasText(cachedCode)) {
            throw new BusinessException("captcha is expired");
        }
        if (!cachedCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("captcha is invalid");
        }
        session.removeAttribute(SESSION_ADMIN_CAPTCHA);
    }

    private AdminSessionVO buildSessionVO() {
        AdminSessionVO vo = new AdminSessionVO();
        vo.setUsername(adminAuthProperties.getUsername());
        vo.setDisplayName(adminAuthProperties.getDisplayName());
        return vo;
    }
}
