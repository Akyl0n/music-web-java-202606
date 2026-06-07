package com.yinyu.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;

    private String password;

    private String nickname;

    private String avatar;

    private String gender;

    private String email;

    private String signature;

    private String status;

    private LocalDateTime lastLoginTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
