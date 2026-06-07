package com.yinyu.mapper;

import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.po.Singer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SingerMapper {

    List<Singer> selectPage(SingerQueryRequest request);

    Long countPage(SingerQueryRequest request);

    Singer selectById(@Param("id") Long id);

    List<Singer> selectEnabledHomeList(@Param("limit") int limit);

    Integer countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(Singer entity);

    int update(Singer entity);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") String status);
}
