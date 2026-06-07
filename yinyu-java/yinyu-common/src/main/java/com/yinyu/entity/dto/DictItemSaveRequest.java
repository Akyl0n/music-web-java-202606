package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DictItemSaveRequest {

    private Long id;

    @NotNull(message = "parentId can not be null")
    private Long parentId;

    @NotBlank(message = "code can not be blank")
    private String code;

    @NotBlank(message = "name can not be blank")
    private String name;

    @NotBlank(message = "status can not be blank")
    private String status;

    private String remark;
}
