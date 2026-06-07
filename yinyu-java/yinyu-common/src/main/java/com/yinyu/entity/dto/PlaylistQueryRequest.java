package com.yinyu.entity.dto;

import lombok.Data;

@Data
public class PlaylistQueryRequest {

    private String keyword;

    private String category;

    private String status;

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    public int getOffset() {
        int currentPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int currentPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        return (currentPageNo - 1) * currentPageSize;
    }

    public int getLimit() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
