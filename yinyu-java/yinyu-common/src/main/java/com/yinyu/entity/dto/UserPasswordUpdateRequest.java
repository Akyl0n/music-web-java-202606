package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPasswordUpdateRequest {

    @NotBlank(message = "old password can not be blank")
    private String oldPassword;

    @NotBlank(message = "new password can not be blank")
    @Size(min = 6, max = 32, message = "new password length must be between 6 and 32")
    private String newPassword;

    @NotBlank(message = "confirm password can not be blank")
    private String confirmPassword;
}
