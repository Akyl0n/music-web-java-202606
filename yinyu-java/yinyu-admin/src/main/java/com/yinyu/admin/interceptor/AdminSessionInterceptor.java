package com.yinyu.admin.interceptor;

import com.yinyu.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminSessionInterceptor implements HandlerInterceptor {

    public static final String SESSION_ADMIN_USERNAME = "admin:auth:username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_ADMIN_USERNAME) == null) {
            throw new BusinessException(401, "please login first");
        }
        return true;
    }
}
