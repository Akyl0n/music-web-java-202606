package com.yinyu.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "nickname can not be blank")
    private String nickname;

    @Email(message = "email format is invalid")
    private String email;

    @NotBlank(message = "password can not be blank")
    private String password;

    @NotBlank(message = "confirmPassword can not be blank")
    private String confirmPassword;

    @NotBlank(message = "captchaCode can not be blank")
    private String captchaCode;
}
