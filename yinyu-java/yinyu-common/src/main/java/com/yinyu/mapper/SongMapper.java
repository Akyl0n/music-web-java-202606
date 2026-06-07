package com.yinyu.mapper;

import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.po.Song;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SongMapper {

    List<Song> selectPage(SongQueryRequest request);

    Long countPage(SongQueryRequest request);

    Song selectById(@Param("id") Long id);

    List<Song> selectByIds(@Param("ids") List<Long> ids);

    List<Song> selectEnabledRankingList();

    List<Song> selectEnabledHomeList(@Param("limit") int limit);

    Song selectFirstEnabledBySingerId(@Param("singerId") Long singerId);

    Integer countByNameAndSingerId(@Param("name") String name, @Param("singerId") Long singerId, @Param("excludeId") Long excludeId);

    int insert(Song entity);

    int update(Song entity);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int updatePlayCount(@Param("id") Long id, @Param("delta") int delta);
}
