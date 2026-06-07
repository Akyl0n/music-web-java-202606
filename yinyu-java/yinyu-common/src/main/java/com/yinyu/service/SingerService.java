package com.yinyu.service;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.dto.SingerSaveRequest;
import com.yinyu.entity.dto.SingerStatusRequest;
import com.yinyu.entity.vo.SingerVO;

public interface SingerService {

    ListData<SingerVO> listPage(SingerQueryRequest request);

    SingerVO getById(Long id);

    void create(SingerSaveRequest request);

    void update(SingerSaveRequest request);

    void delete(Long id);

    void updateStatus(SingerStatusRequest request);
}
