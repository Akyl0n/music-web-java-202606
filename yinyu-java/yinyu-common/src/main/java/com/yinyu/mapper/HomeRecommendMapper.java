package com.yinyu.mapper;

import com.yinyu.entity.dto.HomeRecommendQueryRequest;
import com.yinyu.entity.po.HomeRecommend;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HomeRecommendMapper {

    List<HomeRecommend> selectPage(HomeRecommendQueryRequest request);

    Long countPage(HomeRecommendQueryRequest request);

    HomeRecommend selectById(@Param("id") Long id);

    Integer countByPositionAndTarget(
            @Param("positionCode") String positionCode,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("excludeId") Long excludeId
    );

    Integer selectMaxSortNum(@Param("positionCode") String positionCode);

    int insert(HomeRecommend entity);

    int update(HomeRecommend entity);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    int updateSort(@Param("id") Long id, @Param("sortNum") Integer sortNum);

    List<HomeRecommend> selectEnabledByPosition(@Param("positionCode") String positionCode);

    Long countEnabledByPosition(@Param("positionCode") String positionCode);
}
