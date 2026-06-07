package com.yinyu.web.support;

import com.yinyu.entity.vo.HomePageVO;
import com.yinyu.entity.vo.HomeSongCardVO;
import com.yinyu.entity.vo.PlaylistDetailVO;
import com.yinyu.entity.vo.SingerDetailVO;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.entity.vo.UserLibraryVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSongMediaSupport {

    public String buildSongMediaUrl(Long songId) {
        return songId == null ? "" : "/api/web/songs/" + songId + "/media";
    }

    public void apply(HomePageVO homePageVO) {
        if (homePageVO == null) {
            return;
        }
        applySongCardList(homePageVO.getFeaturedSongs());
        applySongCardList(homePageVO.getHotSongs());
        applySongCardList(homePageVO.getPlayQueue());
    }

    public void apply(PlaylistDetailVO detailVO) {
        if (detailVO == null || detailVO.getSongs() == null) {
            return;
        }
        detailVO.getSongs().forEach(this::applySongVO);
    }

    public void apply(SingerDetailVO detailVO) {
        if (detailVO == null || detailVO.getSongs() == null) {
            return;
        }
        detailVO.getSongs().forEach(this::applySongVO);
    }

    public void apply(UserLibraryVO libraryVO) {
        if (libraryVO == null) {
            return;
        }
        if (libraryVO.getLikedSongs() != null) {
            libraryVO.getLikedSongs().forEach(this::applySongVO);
        }
        if (libraryVO.getRecentSongs() != null) {
            libraryVO.getRecentSongs().forEach(this::applySongVO);
        }
    }

    public void apply(List<SongVO> songs) {
        if (songs == null) {
            return;
        }
        songs.forEach(this::applySongVO);
    }

    private void applySongCardList(List<HomeSongCardVO> list) {
        if (list == null) {
            return;
        }
        list.forEach(this::applySongCard);
    }

    private void applySongCard(HomeSongCardVO songCardVO) {
        if (songCardVO == null) {
            return;
        }
        songCardVO.setAudioUrl(buildSongMediaUrl(songCardVO.getId()));
    }

    private void applySongVO(SongVO songVO) {
        if (songVO == null) {
            return;
        }
        songVO.setAudioUrl(buildSongMediaUrl(songVO.getId()));
    }
}
