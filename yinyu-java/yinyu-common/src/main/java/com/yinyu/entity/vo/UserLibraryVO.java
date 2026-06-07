package com.yinyu.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserLibraryVO {

    private UserVO user;

    private Long likeSongCount;

    private Long favoritePlaylistCount;

    private Long playHistoryCount;

    private List<SongVO> likedSongs;

    private List<SongVO> recentSongs;

    private List<PlaylistVO> favoritePlaylists;
}
