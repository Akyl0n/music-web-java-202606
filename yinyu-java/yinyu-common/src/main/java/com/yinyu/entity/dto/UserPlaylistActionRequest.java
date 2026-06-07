package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPlaylistActionRequest {

    @NotNull(message = "playlistId can not be null")
    private Long playlistId;
}
