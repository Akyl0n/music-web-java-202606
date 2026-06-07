package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.entity.vo.HomePageVO;
import com.yinyu.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yinyu.web.support.WebSongMediaSupport;

@RestController
@RequestMapping("/web/home")
public class WebHomeController {

    private final HomeService homeService;
    private final WebSongMediaSupport webSongMediaSupport;

    public WebHomeController(HomeService homeService, WebSongMediaSupport webSongMediaSupport) {
        this.homeService = homeService;
        this.webSongMediaSupport = webSongMediaSupport;
    }

    @GetMapping
    public ApiResponse<HomePageVO> detail() {
        HomePageVO homePageVO = homeService.getHomePage();
        webSongMediaSupport.apply(homePageVO);
        return ApiResponse.success(homePageVO);
    }
}
