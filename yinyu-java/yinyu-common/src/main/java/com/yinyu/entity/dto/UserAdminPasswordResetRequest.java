package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAdminPasswordResetRequest {

    @NotNull(message = "id can not be null")
    private Long id;

    @NotBlank(message = "newPassword can not be blank")
    private String newPassword;
}
