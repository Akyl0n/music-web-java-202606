package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @NotBlank(message = "nickname can not be blank")
    @Size(max = 32, message = "nickname length can not exceed 32")
    private String nickname;

    @Size(max = 64, message = "email length can not exceed 64")
    private String email;

    @Size(max = 16, message = "gender length can not exceed 16")
    private String gender;

    @Size(max = 255, message = "avatar length can not exceed 255")
    private String avatar;

    @Size(max = 255, message = "signature length can not exceed 255")
    private String signature;
}
