package com.yinyu.entity.po;

import lombok.Data;

@Data
public class SysDict {

    private Long id;

    private Long parentId;

    private String dictCode;

    private String dictName;

    private Integer sortNum;

    private String status;

    private String remark;
}
