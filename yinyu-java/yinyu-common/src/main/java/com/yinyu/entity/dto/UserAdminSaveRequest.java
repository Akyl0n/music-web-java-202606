package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAdminSaveRequest {

    @NotNull(message = "id can not be null")
    private Long id;

    @NotBlank(message = "nickname can not be blank")
    private String nickname;

    private String avatar;

    private String gender;

    private String email;

    private String signature;

    @NotBlank(message = "status can not be blank")
    private String status;

    private String remark;
}
