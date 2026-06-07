package com.yinyu.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Singer {

    private Long id;

    private String name;

    private String gender;

    private LocalDate birthday;

    private String region;

    private String type;

    private String avatar;

    private String intro;

    private String tags;

    private String letter;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
