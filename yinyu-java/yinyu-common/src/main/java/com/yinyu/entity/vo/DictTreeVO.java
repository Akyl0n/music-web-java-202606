package com.yinyu.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DictTreeVO {

    private Long id;

    private Long parentId;

    private String code;

    private String name;

    private Integer sort;

    private String status;

    private String remark;

    private List<DictTreeVO> children = new ArrayList<>();
}
