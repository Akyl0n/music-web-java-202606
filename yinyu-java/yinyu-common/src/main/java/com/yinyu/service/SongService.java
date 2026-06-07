package com.yinyu.service;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.dto.SongSaveRequest;
import com.yinyu.entity.dto.SongStatusRequest;
import com.yinyu.entity.vo.SongVO;

public interface SongService {

    ListData<SongVO> listPage(SongQueryRequest request);

    void create(SongSaveRequest request);

    void update(SongSaveRequest request);

    void delete(Long id);

    void updateStatus(SongStatusRequest request);
}
