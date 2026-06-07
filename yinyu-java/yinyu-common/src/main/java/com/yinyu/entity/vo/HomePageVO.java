package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class HomePageVO {

    private List<HomePlaylistCardVO> banners;

    private List<HomeSongCardVO> featuredSongs;

    private List<HomeTrendVO> trends;

    private List<HomePlaylistCardVO> recommendedPlaylists;

    private List<HomeSongCardVO> hotSongs;

    private List<HomeSingerCardVO> hotSingers;

    private List<HomeCategoryCardVO> categories;

    private List<HomeSongCardVO> playQueue;
}
