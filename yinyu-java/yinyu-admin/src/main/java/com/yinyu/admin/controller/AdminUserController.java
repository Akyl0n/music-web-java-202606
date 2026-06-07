package com.yinyu.admin.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.UserAdminPasswordResetRequest;
import com.yinyu.entity.dto.UserAdminQueryRequest;
import com.yinyu.entity.dto.UserAdminSaveRequest;
import com.yinyu.entity.dto.UserAdminStatusRequest;
import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.entity.vo.UserAdminVO;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public AdminUserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<ListData<UserAdminVO>> list(@Valid UserAdminQueryRequest request) {
        return ApiResponse.success(userService.listAdminPage(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserAdminVO> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.getAdminDetail(id));
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody @Valid UserAdminSaveRequest request) {
        userService.updateAdmin(request);
        return ApiResponse.success("user updated", null);
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@RequestBody @Valid UserAdminStatusRequest request) {
        userService.updateAdminStatus(request);
        return ApiResponse.success("user status updated", null);
    }

    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid UserAdminPasswordResetRequest request) {
        userService.resetAdminPassword(request);
        return ApiResponse.success("user password reset", null);
    }

    @PostMapping("/avatar")
    public ApiResponse<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeImage(file, "user-avatar"));
    }
}
