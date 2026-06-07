package com.yinyu.admin.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.dto.PlaylistSaveRequest;
import com.yinyu.entity.dto.PlaylistStatusRequest;
import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.entity.vo.PlaylistVO;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/admin/playlists")
public class AdminPlaylistController {

    private final PlaylistService playlistService;
    private final FileStorageService fileStorageService;

    public AdminPlaylistController(PlaylistService playlistService, FileStorageService fileStorageService) {
        this.playlistService = playlistService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<ListData<PlaylistVO>> list(@Valid PlaylistQueryRequest request) {
        return ApiResponse.success(playlistService.listPage(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlaylistVO> detail(@PathVariable Long id) {
        return ApiResponse.success(playlistService.detail(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid PlaylistSaveRequest request) {
        playlistService.create(request);
        return ApiResponse.success("playlist created", null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody @Valid PlaylistSaveRequest request) {
        playlistService.update(request);
        return ApiResponse.success("playlist updated", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        playlistService.delete(id);
        return ApiResponse.success("playlist deleted", null);
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@RequestBody @Valid PlaylistStatusRequest request) {
        playlistService.updateStatus(request);
        return ApiResponse.success("playlist status updated", null);
    }

    @PostMapping("/cover")
    public ApiResponse<FileUploadVO> uploadCover(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeImage(file, "playlist-cover"));
    }
}
