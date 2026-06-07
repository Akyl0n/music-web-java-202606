package com.yinyu.service;

import com.yinyu.entity.dto.UserLoginRequest;
import com.yinyu.entity.dto.UserAdminPasswordResetRequest;
import com.yinyu.entity.dto.UserAdminQueryRequest;
import com.yinyu.entity.dto.UserAdminSaveRequest;
import com.yinyu.entity.dto.UserAdminStatusRequest;
import com.yinyu.entity.dto.UserPasswordUpdateRequest;
import com.yinyu.entity.dto.UserPlaylistActionRequest;
import com.yinyu.entity.dto.UserProfileUpdateRequest;
import com.yinyu.entity.dto.UserRegisterRequest;
import com.yinyu.entity.dto.UserSongActionRequest;
import com.yinyu.api.ListData;
import com.yinyu.entity.vo.UserAdminVO;
import com.yinyu.entity.vo.UserCaptchaVO;
import com.yinyu.entity.vo.UserLibraryVO;
import com.yinyu.entity.vo.UserVO;
import jakarta.servlet.http.HttpSession;

public interface UserService {

    ListData<UserAdminVO> listAdminPage(UserAdminQueryRequest request);

    UserAdminVO getAdminDetail(Long id);

    void updateAdmin(UserAdminSaveRequest request);

    void updateAdminStatus(UserAdminStatusRequest request);

    void resetAdminPassword(UserAdminPasswordResetRequest request);

    UserCaptchaVO generateCaptcha(HttpSession session);

    UserVO register(UserRegisterRequest request, HttpSession session);

    UserVO login(UserLoginRequest request, HttpSession session);

    void logout(HttpSession session);

    UserVO getCurrentUser(HttpSession session);

    UserVO updateProfile(UserProfileUpdateRequest request, HttpSession session);

    void updatePassword(UserPasswordUpdateRequest request, HttpSession session);

    UserLibraryVO getLibrary(HttpSession session);

    void likeSong(UserSongActionRequest request, HttpSession session);

    void unlikeSong(Long songId, HttpSession session);

    void favoritePlaylist(UserPlaylistActionRequest request, HttpSession session);

    void unfavoritePlaylist(Long playlistId, HttpSession session);

    void recordPlaySong(UserSongActionRequest request, HttpSession session);
}
