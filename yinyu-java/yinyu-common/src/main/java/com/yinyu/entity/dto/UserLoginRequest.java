package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @NotBlank(message = "account can not be blank")
    private String account;

    @NotBlank(message = "password can not be blank")
    private String password;

    @NotBlank(message = "captchaCode can not be blank")
    private String captchaCode;
}
