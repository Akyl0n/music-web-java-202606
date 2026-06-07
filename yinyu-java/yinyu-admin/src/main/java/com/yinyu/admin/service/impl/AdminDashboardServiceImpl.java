package com.yinyu.admin.service.impl;

import com.yinyu.admin.service.AdminDashboardService;
import com.yinyu.entity.dto.HomeRecommendQueryRequest;
import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.dto.UserAdminQueryRequest;
import com.yinyu.entity.enums.UserActionTypeEnum;
import com.yinyu.entity.vo.AdminDashboardMetricVO;
import com.yinyu.entity.vo.AdminDashboardNoticeVO;
import com.yinyu.entity.vo.AdminDashboardStatVO;
import com.yinyu.entity.vo.AdminDashboardVO;
import com.yinyu.mapper.HomeRecommendMapper;
import com.yinyu.mapper.PlaylistMapper;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.mapper.UserActionMapper;
import com.yinyu.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final SongMapper songMapper;
    private final PlaylistMapper playlistMapper;
    private final SingerMapper singerMapper;
    private final UserMapper userMapper;
    private final HomeRecommendMapper homeRecommendMapper;
    private final UserActionMapper userActionMapper;

    public AdminDashboardServiceImpl(
        SongMapper songMapper,
        PlaylistMapper playlistMapper,
        SingerMapper singerMapper,
        UserMapper userMapper,
        HomeRecommendMapper homeRecommendMapper,
        UserActionMapper userActionMapper
    ) {
        this.songMapper = songMapper;
        this.playlistMapper = playlistMapper;
        this.singerMapper = singerMapper;
        this.userMapper = userMapper;
        this.homeRecommendMapper = homeRecommendMapper;
        this.userActionMapper = userActionMapper;
    }

    @Override
    public AdminDashboardVO getDashboard() {
        long totalSongs = countSongs(null);
        long enabledSongs = countSongs("enabled");
        long totalPlaylists = countPlaylists(null);
        long enabledPlaylists = countPlaylists("enabled");
        long totalSingers = countSingers(null);
        long enabledSingers = countSingers("enabled");
        long totalUsers = countUsers(null);
        long enabledUsers = countUsers("enabled");
        long recentLoginUsers = safeLong(userMapper.countRecentLogin(7));
        long bannerCount = safeLong(homeRecommendMapper.countEnabledByPosition("home_banner"));
        long dailyRecommendCount = safeLong(homeRecommendMapper.countEnabledByPosition("home_daily_song"));
        long likeCount = safeLong(userActionMapper.countByType(UserActionTypeEnum.LIKE_SONG.getCode()));
        long favoriteCount = safeLong(userActionMapper.countByType(UserActionTypeEnum.FAVORITE_PLAYLIST.getCode()));
        long playHistoryCount = safeLong(userActionMapper.countByType(UserActionTypeEnum.PLAY_SONG.getCode()));

        AdminDashboardVO vo = new AdminDashboardVO();
        vo.setMetrics(List.of(
            metric("内容资源", "歌曲总数", totalSongs, "已上架歌曲", enabledSongs, "blue",
                trend(totalSongs, enabledSongs, totalPlaylists, enabledPlaylists, totalSingers, enabledSingers, bannerCount)),
            metric("用户概览", "用户总数", totalUsers, "近 7 天登录", recentLoginUsers, "green",
                trend(totalUsers, enabledUsers, recentLoginUsers, favoriteCount, likeCount, playHistoryCount, enabledUsers)),
            metric("首页运营", "轮播配置", bannerCount, "今日推荐", dailyRecommendCount, "gold",
                trend(bannerCount, dailyRecommendCount, enabledPlaylists, enabledSongs, enabledSingers, favoriteCount, likeCount)),
            metric("互动行为", "喜欢记录", likeCount, "播放历史", playHistoryCount, "purple",
                trend(likeCount, favoriteCount, playHistoryCount, totalUsers, recentLoginUsers, enabledSongs, enabledPlaylists))
        ));

        vo.setContentStats(List.of(
            stat("歌曲管理", value(totalSongs), "已上架 " + enabledSongs + " 首，待处理 " + Math.max(totalSongs - enabledSongs, 0) + " 首"),
            stat("歌单管理", value(totalPlaylists), "已启用 " + enabledPlaylists + " 张歌单"),
            stat("歌手管理", value(totalSingers), "已启用 " + enabledSingers + " 位歌手"),
            stat("字典管理", "基础数据", "国家、风格、分类统一由字典管理维护")
        ));

        vo.setOperationStats(List.of(
            stat("首页轮播", value(bannerCount), "当前启用的首页轮播数量"),
            stat("今日推荐", value(dailyRecommendCount), "当前启用的今日推荐数量"),
            stat("喜欢记录", value(likeCount), "来自用户喜欢歌曲的累计记录"),
            stat("收藏记录", value(favoriteCount), "来自用户收藏歌单的累计记录")
        ));

        vo.setQuickEntries(List.of(
            notice("歌曲管理", "补充歌曲封面、音频和上下架状态。", "/admin/songs"),
            notice("首页推荐", "维护轮播图与今日推荐的展示内容。", "/admin/recommend"),
            notice("用户管理", "查看用户资料、状态和互动数据。", "/admin/users"),
            notice("字典管理", "统一维护国家、风格、分类等基础数据。", "/admin/dicts")
        ));
        return vo;
    }

    private long countSongs(String status) {
        SongQueryRequest request = new SongQueryRequest();
        request.setStatus(status);
        return safeLong(songMapper.countPage(request));
    }

    private long countPlaylists(String status) {
        PlaylistQueryRequest request = new PlaylistQueryRequest();
        request.setStatus(status);
        return safeLong(playlistMapper.countPage(request));
    }

    private long countSingers(String status) {
        SingerQueryRequest request = new SingerQueryRequest();
        request.setStatus(status);
        return safeLong(singerMapper.countPage(request));
    }

    private long countUsers(String status) {
        UserAdminQueryRequest request = new UserAdminQueryRequest();
        request.setStatus(status);
        return safeLong(userMapper.countAdminPage(request));
    }

    private AdminDashboardMetricVO metric(
        String title,
        String primaryLabel,
        long primaryValue,
        String secondaryLabel,
        long secondaryValue,
        String accent,
        List<Integer> trend
    ) {
        AdminDashboardMetricVO vo = new AdminDashboardMetricVO();
        vo.setTitle(title);
        vo.setPrimaryLabel(primaryLabel);
        vo.setPrimaryValue(value(primaryValue));
        vo.setSecondaryLabel(secondaryLabel);
        vo.setSecondaryValue(value(secondaryValue));
        vo.setAccent(accent);
        vo.setTrend(trend);
        return vo;
    }

    private AdminDashboardStatVO stat(String label, String value, String desc) {
        AdminDashboardStatVO vo = new AdminDashboardStatVO();
        vo.setLabel(label);
        vo.setValue(value);
        vo.setDesc(desc);
        return vo;
    }

    private AdminDashboardNoticeVO notice(String title, String desc, String path) {
        AdminDashboardNoticeVO vo = new AdminDashboardNoticeVO();
        vo.setTitle(title);
        vo.setDesc(desc);
        vo.setPath(path);
        return vo;
    }

    private List<Integer> trend(long... values) {
        return java.util.Arrays.stream(values)
            .mapToInt(value -> {
                long safeValue = Math.max(value, 1);
                long scaled = Math.min(72, 18 + safeValue % 54);
                return (int) scaled;
            })
            .boxed()
            .toList();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String value(long value) {
        return String.valueOf(value);
    }
}
