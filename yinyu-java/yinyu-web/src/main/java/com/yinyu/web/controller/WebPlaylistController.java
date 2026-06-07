package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.vo.PlaylistDetailVO;
import com.yinyu.entity.vo.PlaylistVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.service.PlaylistService;
import com.yinyu.web.support.WebSongMediaSupport;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/web/playlists")
public class WebPlaylistController {

    private final PlaylistService playlistService;
    private final WebSongMediaSupport webSongMediaSupport;

    public WebPlaylistController(PlaylistService playlistService, WebSongMediaSupport webSongMediaSupport) {
        this.playlistService = playlistService;
        this.webSongMediaSupport = webSongMediaSupport;
    }

    @GetMapping
    public ApiResponse<ListData<PlaylistVO>> list(PlaylistQueryRequest request) {
        PlaylistQueryRequest query = request == null ? new PlaylistQueryRequest() : request;
        query.setStatus("enabled");
        return ApiResponse.success(playlistService.listPage(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlaylistDetailVO> detail(@PathVariable Long id) {
        PlaylistDetailVO detail = playlistService.detailWithSongs(id);
        if (detail.getPlaylist() == null || !"enabled".equalsIgnoreCase(detail.getPlaylist().getStatus())) {
            throw new BusinessException("playlist not found");
        }
        webSongMediaSupport.apply(detail);
        return ApiResponse.success(detail);
    }
}
