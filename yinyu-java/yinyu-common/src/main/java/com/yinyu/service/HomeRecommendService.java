package com.yinyu.service;

import com.yinyu.api.ListData;
import com.yinyu.entity.dto.HomeRecommendQueryRequest;
import com.yinyu.entity.dto.HomeRecommendSaveRequest;
import com.yinyu.entity.dto.HomeRecommendSortRequest;
import com.yinyu.entity.dto.HomeRecommendStatusRequest;
import com.yinyu.entity.vo.HomeRecommendTargetOptionVO;
import com.yinyu.entity.vo.HomeRecommendVO;

import java.util.List;

public interface HomeRecommendService {

    ListData<HomeRecommendVO> listPage(HomeRecommendQueryRequest request);

    HomeRecommendVO detail(Long id);

    void create(HomeRecommendSaveRequest request);

    void update(HomeRecommendSaveRequest request);

    void delete(Long id);

    void updateStatus(HomeRecommendStatusRequest request);

    void sort(HomeRecommendSortRequest request);

    List<HomeRecommendVO> listEnabledByPosition(String positionCode);

    List<HomeRecommendTargetOptionVO> listTargetOptions(String targetType);
}
