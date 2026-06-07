package com.yinyu.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "admin.auth")
public class AdminAuthProperties {

    private String username;

    private String password;

    private String displayName = "管理员";
}
