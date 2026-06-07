package com.yinyu.admin.config;

import com.yinyu.admin.interceptor.AdminSessionInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AdminAuthProperties.class)
public class AdminWebMvcConfig implements WebMvcConfigurer {

    private final AdminSessionInterceptor adminSessionInterceptor;

    public AdminWebMvcConfig(AdminSessionInterceptor adminSessionInterceptor) {
        this.adminSessionInterceptor = adminSessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminSessionInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns(
                "/admin/auth/captcha",
                "/admin/auth/login",
                "/admin/auth/logout"
            );
    }
}
