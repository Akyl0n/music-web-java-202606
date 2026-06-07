package com.yinyu.entity.vo;

import lombok.Data;

@Data
public class DictItemVO {

    private Long id;

    private Long parentId;

    private String code;

    private String name;

    private Integer sort;

    private String status;

    private String remark;
}
