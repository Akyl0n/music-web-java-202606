package com.yinyu.mapper;

import com.yinyu.entity.po.SysDict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDictMapper {

    List<SysDict> selectTypeList(@Param("keyword") String keyword);

    List<SysDict> selectItemList(@Param("parentId") Long parentId, @Param("keyword") String keyword);

    SysDict selectById(@Param("id") Long id);

    Integer countByParentIdAndCode(
            @Param("parentId") Long parentId,
            @Param("dictCode") String dictCode,
            @Param("excludeId") Long excludeId
    );

    Integer selectMaxSortNumByParentId(@Param("parentId") Long parentId);

    int insert(SysDict entity);

    int update(SysDict entity);

    int deleteById(@Param("id") Long id);

    int deleteByParentId(@Param("parentId") Long parentId);

    int updateSort(@Param("id") Long id, @Param("sortNum") Integer sortNum);

    List<SysDict> selectEnabledTypes(@Param("code") String code);

    List<SysDict> selectEnabledItems(@Param("parentId") Long parentId);

    List<SysDict> selectEnabledItemsByTypeCode(@Param("typeCode") String typeCode);
}
