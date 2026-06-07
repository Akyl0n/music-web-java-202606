package com.yinyu.mapper;

import com.yinyu.entity.po.UserAction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserActionMapper {

    UserAction selectByUserIdAndTypeAndTargetId(@Param("userId") Long userId, @Param("actionType") String actionType, @Param("targetId") Long targetId);

    int insert(UserAction entity);

    int updateAction(UserAction entity);

    int deleteByUserIdAndTypeAndTargetId(@Param("userId") Long userId, @Param("actionType") String actionType, @Param("targetId") Long targetId);

    Long countByUserIdAndType(@Param("userId") Long userId, @Param("actionType") String actionType);

    List<Long> selectTargetIdsByUserIdAndType(@Param("userId") Long userId, @Param("actionType") String actionType, @Param("limit") Integer limit);

    Long countByType(@Param("actionType") String actionType);
}
