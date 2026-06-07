package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistSaveRequest {

    private Long id;

    @NotBlank(message = "name can not be blank")
    private String name;

    private String subtitle;

    private String cover;

    private String category;

    private String tags;

    private String intro;

    @NotBlank(message = "status can not be blank")
    private String status;

    private Integer recommendFlag;

    private String remark;

    private List<Long> songIds;
}
