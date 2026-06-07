package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.vo.SingerDetailVO;
import com.yinyu.entity.vo.SingerVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.service.SingerService;
import com.yinyu.service.SongService;
import com.yinyu.web.support.WebSongMediaSupport;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/web/singers")
public class WebSingerController {

    private final SingerService singerService;
    private final SongService songService;
    private final WebSongMediaSupport webSongMediaSupport;

    public WebSingerController(SingerService singerService, SongService songService, WebSongMediaSupport webSongMediaSupport) {
        this.singerService = singerService;
        this.songService = songService;
        this.webSongMediaSupport = webSongMediaSupport;
    }

    @GetMapping
    public ApiResponse<ListData<SingerVO>> list(SingerQueryRequest request) {
        SingerQueryRequest query = request == null ? new SingerQueryRequest() : request;
        query.setStatus("enabled");
        return ApiResponse.success(singerService.listPage(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<SingerDetailVO> detail(@PathVariable Long id) {
        SingerVO singer = singerService.getById(id);
        if (!"enabled".equalsIgnoreCase(singer.getStatus())) {
            throw new BusinessException("singer not found");
        }

        SongQueryRequest query = new SongQueryRequest();
        query.setSingerId(id);
        query.setStatus("enabled");
        query.setPageNo(1);
        query.setPageSize(100);
        ListData<SongVO> songData = songService.listPage(query);

        SingerDetailVO vo = new SingerDetailVO();
        vo.setSinger(singer);
        vo.setSongs(songData.getList());
        vo.setTotalSongs(songData.getTotal());
        webSongMediaSupport.apply(vo);
        return ApiResponse.success(vo);
    }
}
