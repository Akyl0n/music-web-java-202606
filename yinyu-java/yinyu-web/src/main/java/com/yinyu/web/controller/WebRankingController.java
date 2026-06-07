package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.entity.vo.RankingBoardVO;
import com.yinyu.entity.vo.RankingDetailVO;
import com.yinyu.service.RankingService;
import com.yinyu.web.support.WebSongMediaSupport;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/web/rankings")
public class WebRankingController {

    private final RankingService rankingService;
    private final WebSongMediaSupport webSongMediaSupport;

    public WebRankingController(RankingService rankingService, WebSongMediaSupport webSongMediaSupport) {
        this.rankingService = rankingService;
        this.webSongMediaSupport = webSongMediaSupport;
    }

    @GetMapping
    public ApiResponse<List<RankingBoardVO>> listBoards() {
        return ApiResponse.success(rankingService.listBoards());
    }

    @GetMapping("/{code}")
    public ApiResponse<RankingDetailVO> detail(@PathVariable String code) {
        RankingDetailVO detail = rankingService.getDetail(code);
        webSongMediaSupport.apply(detail.getSongs());
        return ApiResponse.success(detail);
    }
}
