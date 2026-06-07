package com.yinyu.service;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.dto.PlaylistSaveRequest;
import com.yinyu.entity.dto.PlaylistStatusRequest;
import com.yinyu.entity.vo.PlaylistDetailVO;
import com.yinyu.entity.vo.PlaylistVO;

public interface PlaylistService {

    ListData<PlaylistVO> listPage(PlaylistQueryRequest request);

    PlaylistVO detail(Long id);

    PlaylistDetailVO detailWithSongs(Long id);

    void create(PlaylistSaveRequest request);

    void update(PlaylistSaveRequest request);

    void delete(Long id);

    void updateStatus(PlaylistStatusRequest request);
}
