package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SongSaveRequest {

    private Long id;

    @NotBlank(message = "name can not be blank")
    private String name;

    private String subtitle;

    @NotNull(message = "singerId can not be null")
    private Long singerId;

    private String category;

    private String tags;

    private String cover;

    private String audioUrl;

    private Integer durationSeconds;

    private String language;

    private String intro;

    private LocalDate releaseDate;

    @NotBlank(message = "status can not be blank")
    private String status;

    private Integer recommendFlag;

    private String remark;
}
