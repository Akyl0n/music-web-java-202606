package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DictItemQueryRequest {

    @NotNull(message = "parentId can not be null")
    private Long parentId;

    private String keyword;
}
