package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SingerSaveRequest {

    private Long id;

    @NotBlank(message = "name can not be blank")
    private String name;

    private String gender;

    private LocalDate birthday;

    private String region;

    @NotBlank(message = "type can not be blank")
    private String type;

    private String avatar;

    private String intro;

    private String tags;

    private String letter;

    @NotBlank(message = "status can not be blank")
    private String status;

    private String remark;
}
