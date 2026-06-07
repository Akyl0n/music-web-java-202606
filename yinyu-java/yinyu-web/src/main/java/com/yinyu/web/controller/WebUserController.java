package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.entity.dto.UserPasswordUpdateRequest;
import com.yinyu.entity.dto.UserLoginRequest;
import com.yinyu.entity.dto.UserPlaylistActionRequest;
import com.yinyu.entity.dto.UserProfileUpdateRequest;
import com.yinyu.entity.dto.UserRegisterRequest;
import com.yinyu.entity.dto.UserSongActionRequest;
import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.entity.vo.UserCaptchaVO;
import com.yinyu.entity.vo.UserLibraryVO;
import com.yinyu.entity.vo.UserVO;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.UserService;
import com.yinyu.web.support.WebSongMediaSupport;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/web/users")
public class WebUserController {

    private final UserService userService;
    private final WebSongMediaSupport webSongMediaSupport;
    private final FileStorageService fileStorageService;

    public WebUserController(UserService userService, WebSongMediaSupport webSongMediaSupport, FileStorageService fileStorageService) {
        this.userService = userService;
        this.webSongMediaSupport = webSongMediaSupport;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/captcha")
    public ApiResponse<UserCaptchaVO> captcha(HttpSession session) {
        return ApiResponse.success(userService.generateCaptcha(session));
    }

    @PostMapping("/register")
    public ApiResponse<UserVO> register(@RequestBody @Valid UserRegisterRequest request, HttpSession session) {
        return ApiResponse.success("register success", userService.register(request, session));
    }

    @PostMapping("/login")
    public ApiResponse<UserVO> login(@RequestBody @Valid UserLoginRequest request, HttpSession session) {
        return ApiResponse.success("login success", userService.login(request, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        userService.logout(session);
        return ApiResponse.success("logout success", null);
    }

    @GetMapping("/current")
    public ApiResponse<UserVO> current(HttpSession session) {
        return ApiResponse.success(userService.getCurrentUser(session));
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@RequestBody @Valid UserProfileUpdateRequest request, HttpSession session) {
        return ApiResponse.success("profile updated", userService.updateProfile(request, session));
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestBody @Valid UserPasswordUpdateRequest request, HttpSession session) {
        userService.updatePassword(request, session);
        return ApiResponse.success("password updated", null);
    }

    @PostMapping("/avatar")
    public ApiResponse<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeImage(file, "user-avatar"));
    }

    @GetMapping("/library")
    public ApiResponse<UserLibraryVO> library(HttpSession session) {
        UserLibraryVO library = userService.getLibrary(session);
        webSongMediaSupport.apply(library);
        return ApiResponse.success(library);
    }

    @PostMapping("/actions/songs/like")
    public ApiResponse<Void> likeSong(@RequestBody @Valid UserSongActionRequest request, HttpSession session) {
        userService.likeSong(request, session);
        return ApiResponse.success("song liked", null);
    }

    @DeleteMapping("/actions/songs/like/{songId}")
    public ApiResponse<Void> unlikeSong(@PathVariable Long songId, HttpSession session) {
        userService.unlikeSong(songId, session);
        return ApiResponse.success("song unliked", null);
    }

    @PostMapping("/actions/playlists/favorite")
    public ApiResponse<Void> favoritePlaylist(@RequestBody @Valid UserPlaylistActionRequest request, HttpSession session) {
        userService.favoritePlaylist(request, session);
        return ApiResponse.success("playlist favorited", null);
    }

    @DeleteMapping("/actions/playlists/favorite/{playlistId}")
    public ApiResponse<Void> unfavoritePlaylist(@PathVariable Long playlistId, HttpSession session) {
        userService.unfavoritePlaylist(playlistId, session);
        return ApiResponse.success("playlist unfavorited", null);
    }

    @PostMapping("/actions/songs/play")
    public ApiResponse<Void> playSong(@RequestBody @Valid UserSongActionRequest request, HttpSession session) {
        userService.recordPlaySong(request, session);
        return ApiResponse.success("play record updated", null);
    }
}
