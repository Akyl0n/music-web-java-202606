package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSongActionRequest {

    @NotNull(message = "songId can not be null")
    private Long songId;

    private Integer progressSeconds;
}
