package com.yinyu.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictTypeSaveRequest {

    private Long id;

    @NotBlank(message = "code can not be blank")
    private String code;

    @NotBlank(message = "name can not be blank")
    private String name;

    @NotBlank(message = "status can not be blank")
    private String status;

    private String remark;
}
