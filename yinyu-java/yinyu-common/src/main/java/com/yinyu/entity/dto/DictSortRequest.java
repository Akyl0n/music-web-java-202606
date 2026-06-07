package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DictSortRequest {

    private Long parentId;

    @NotEmpty(message = "ids can not be empty")
    private List<Long> ids;
}
