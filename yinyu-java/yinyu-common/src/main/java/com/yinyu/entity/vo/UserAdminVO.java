package com.yinyu.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAdminVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String gender;

    private String email;

    private String signature;

    private String status;

    private String remark;

    private Long likeSongCount;

    private Long favoritePlaylistCount;

    private Long playHistoryCount;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
