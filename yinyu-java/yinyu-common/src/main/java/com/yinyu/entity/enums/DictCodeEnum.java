package com.yinyu.entity.enums;

import com.yinyu.exception.BusinessException;

import java.util.Arrays;

public enum DictCodeEnum {

    COUNTRY("country"),
    STYLE("style"),
    CATEGORY("category");

    private final String code;

    DictCodeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DictCodeEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new BusinessException("dict code is invalid"));
    }
}
