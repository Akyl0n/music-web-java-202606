package com.yinyu.mapper;

import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.po.Playlist;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlaylistMapper {

    List<Playlist> selectPage(PlaylistQueryRequest request);

    Long countPage(PlaylistQueryRequest request);

    Playlist selectById(@Param("id") Long id);

    List<Playlist> selectByIds(@Param("ids") List<Long> ids);

    List<Playlist> selectEnabledHomeList(@Param("limit") int limit);

    Integer countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(Playlist entity);

    int update(Playlist entity);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    int deletePlaylistSongsByPlaylistId(@Param("playlistId") Long playlistId);

    int batchInsertPlaylistSongs(@Param("playlistId") Long playlistId, @Param("songIds") List<Long> songIds);

    List<Long> selectSongIdsByPlaylistId(@Param("playlistId") Long playlistId);

    int updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);
}
