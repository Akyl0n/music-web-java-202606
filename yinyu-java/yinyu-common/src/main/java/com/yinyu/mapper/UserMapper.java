package com.yinyu.mapper;

import com.yinyu.entity.po.User;
import com.yinyu.entity.dto.UserAdminQueryRequest;
import com.yinyu.entity.vo.UserAdminVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByNickname(@Param("nickname") String nickname);

    User selectByEmail(@Param("email") String email);

    User selectByLoginName(@Param("loginName") String loginName);

    int insert(User entity);

    int updateLastLoginTime(@Param("id") Long id);

    int updateProfile(User entity);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    List<UserAdminVO> selectAdminPage(UserAdminQueryRequest request);

    Long countAdminPage(UserAdminQueryRequest request);

    UserAdminVO selectAdminDetail(@Param("id") Long id);

    int updateAdmin(User entity);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    Long countRecentLogin(@Param("days") Integer days);
}
