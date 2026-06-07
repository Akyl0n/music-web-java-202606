package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {

    @NotBlank(message = "username can not be blank")
    private String username;

    @NotBlank(message = "password can not be blank")
    private String password;

    @NotBlank(message = "captchaCode can not be blank")
    private String captchaCode;
}
