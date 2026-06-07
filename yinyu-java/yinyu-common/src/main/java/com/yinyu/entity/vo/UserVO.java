package com.yinyu.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String gender;

    private String email;

    private String signature;

    private String status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;
}
