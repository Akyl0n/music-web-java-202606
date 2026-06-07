package com.yinyu.admin.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.HomeRecommendQueryRequest;
import com.yinyu.entity.dto.HomeRecommendSaveRequest;
import com.yinyu.entity.dto.HomeRecommendSortRequest;
import com.yinyu.entity.dto.HomeRecommendStatusRequest;
import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.entity.vo.HomeRecommendTargetOptionVO;
import com.yinyu.entity.vo.HomeRecommendVO;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.HomeRecommendService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/home-recommends")
public class AdminHomeRecommendController {

    private final HomeRecommendService homeRecommendService;
    private final FileStorageService fileStorageService;

    public AdminHomeRecommendController(HomeRecommendService homeRecommendService, FileStorageService fileStorageService) {
        this.homeRecommendService = homeRecommendService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<ListData<HomeRecommendVO>> list(@Valid HomeRecommendQueryRequest request) {
        return ApiResponse.success(homeRecommendService.listPage(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<HomeRecommendVO> detail(@PathVariable Long id) {
        return ApiResponse.success(homeRecommendService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid HomeRecommendSaveRequest request) {
        homeRecommendService.create(request);
        return ApiResponse.success("home recommend created", null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody @Valid HomeRecommendSaveRequest request) {
        homeRecommendService.update(request);
        return ApiResponse.success("home recommend updated", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        homeRecommendService.delete(id);
        return ApiResponse.success("home recommend deleted", null);
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@RequestBody @Valid HomeRecommendStatusRequest request) {
        homeRecommendService.updateStatus(request);
        return ApiResponse.success("home recommend status updated", null);
    }

    @PutMapping("/sort")
    public ApiResponse<Void> sort(@RequestBody HomeRecommendSortRequest request) {
        homeRecommendService.sort(request);
        return ApiResponse.success("home recommend sorted", null);
    }

    @GetMapping("/options")
    public ApiResponse<List<HomeRecommendTargetOptionVO>> listTargetOptions(@RequestParam String targetType) {
        return ApiResponse.success(homeRecommendService.listTargetOptions(targetType));
    }

    @PostMapping("/cover")
    public ApiResponse<FileUploadVO> uploadCover(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeImage(file, "home-recommend"));
    }
}
