package com.yinyu.admin.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.dto.SingerSaveRequest;
import com.yinyu.entity.dto.SingerStatusRequest;
import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.entity.vo.SingerVO;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.SingerService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/admin/singers")
public class AdminSingerController {

    private final SingerService singerService;
    private final FileStorageService fileStorageService;

    public AdminSingerController(SingerService singerService, FileStorageService fileStorageService) {
        this.singerService = singerService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<ListData<SingerVO>> list(@Valid SingerQueryRequest request) {
        return ApiResponse.success(singerService.listPage(request));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid SingerSaveRequest request) {
        singerService.create(request);
        return ApiResponse.success("singer created", null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody @Valid SingerSaveRequest request) {
        singerService.update(request);
        return ApiResponse.success("singer updated", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        singerService.delete(id);
        return ApiResponse.success("singer deleted", null);
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@RequestBody @Valid SingerStatusRequest request) {
        singerService.updateStatus(request);
        return ApiResponse.success("singer status updated", null);
    }

    @PostMapping("/avatar")
    public ApiResponse<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeImage(file, "singer-avatar"));
    }

}
