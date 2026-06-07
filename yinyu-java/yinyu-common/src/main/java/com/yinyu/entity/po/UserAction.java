package com.yinyu.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAction {

    private Long id;

    private Long userId;

    private String actionType;

    private Long targetId;

    private LocalDateTime actionTime;

    private Integer progressSeconds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
