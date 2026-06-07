package com.yinyu.admin.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.dto.SongSaveRequest;
import com.yinyu.entity.dto.SongStatusRequest;
import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.SongService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/admin/songs")
public class AdminSongController {

    private final SongService songService;
    private final FileStorageService fileStorageService;

    public AdminSongController(SongService songService, FileStorageService fileStorageService) {
        this.songService = songService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<ListData<SongVO>> list(@Valid SongQueryRequest request) {
        return ApiResponse.success(songService.listPage(request));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody @Valid SongSaveRequest request) {
        songService.create(request);
        return ApiResponse.success("song created", null);
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody @Valid SongSaveRequest request) {
        songService.update(request);
        return ApiResponse.success("song updated", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        songService.delete(id);
        return ApiResponse.success("song deleted", null);
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@RequestBody @Valid SongStatusRequest request) {
        songService.updateStatus(request);
        return ApiResponse.success("song status updated", null);
    }

    @PostMapping("/cover")
    public ApiResponse<FileUploadVO> uploadCover(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeImage(file, "song-cover"));
    }

    @PostMapping("/audio")
    public ApiResponse<FileUploadVO> uploadAudio(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileStorageService.storeAudio(file, "song-audio"));
    }
}
