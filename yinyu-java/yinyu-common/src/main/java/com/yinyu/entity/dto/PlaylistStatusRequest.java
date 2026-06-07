package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistStatusRequest {

    @NotEmpty(message = "ids can not be empty")
    private List<Long> ids;

    @NotBlank(message = "status can not be blank")
    private String status;
}
