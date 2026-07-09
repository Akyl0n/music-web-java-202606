package com.yinyu.service.impl;

import com.yinyu.config.RedisCacheConfig;
import com.yinyu.entity.po.Playlist;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.vo.DictItemVO;
import com.yinyu.entity.vo.HomeCategoryCardVO;
import com.yinyu.entity.vo.HomePageVO;
import com.yinyu.entity.vo.HomePlaylistCardVO;
import com.yinyu.entity.vo.HomeRecommendVO;
import com.yinyu.entity.vo.HomeSingerCardVO;
import com.yinyu.entity.vo.HomeSongCardVO;
import com.yinyu.mapper.PlaylistMapper;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import com.yinyu.service.DictService;
import com.yinyu.service.HomeRecommendService;
import com.yinyu.service.HomeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class HomeServiceImpl implements HomeService {

    private static final int BANNER_LIMIT = 5;
    private static final int FEATURED_LIMIT = 3;
    private static final int PLAYLIST_LIMIT = 5;
    private static final int HOT_SONG_LIMIT = 10;
    private static final int HOT_SINGER_LIMIT = 6;
    private static final int CATEGORY_CARD_LIMIT = 6;
    private static final String POSITION_HOME_BANNER = "home_banner";
    private static final String POSITION_HOME_DAILY_SONG = "home_daily_song";
    private static final String TARGET_SONG = "song";
    private static final String TARGET_PLAYLIST = "playlist";
    private static final String TARGET_SINGER = "singer";

    private final PlaylistMapper playlistMapper;
    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final DictService dictService;
    private final HomeRecommendService homeRecommendService;

    public HomeServiceImpl(
            PlaylistMapper playlistMapper,
            SongMapper songMapper,
            SingerMapper singerMapper,
            DictService dictService,
            HomeRecommendService homeRecommendService
    ) {
        this.playlistMapper = playlistMapper;
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.dictService = dictService;
        this.homeRecommendService = homeRecommendService;
    }

    @Override
    @Cacheable(cacheNames = RedisCacheConfig.CACHE_HOME_PAGE, key = "'default'")
    public HomePageVO getHomePage() {
        Map<String, String> categoryNameMap = buildCategoryNameMap();
        List<Playlist> playlistEntities = playlistMapper.selectEnabledHomeList(24);
        List<Song> songEntities = songMapper.selectEnabledHomeList(24);
        List<Singer> singerEntities = singerMapper.selectEnabledHomeList(HOT_SINGER_LIMIT);
        Map<Long, String> singerAvatarMap = buildSingerAvatarMap(singerEntities);

        Map<Long, Song> songMap = new LinkedHashMap<>();
        songEntities.forEach(song -> songMap.put(song.getId(), song));

        List<HomePlaylistCardVO> playlistCards = playlistEntities.stream()
                .map(item -> toPlaylistCard(item, categoryNameMap))
                .toList();
        List<HomeSongCardVO> hotSongs = songEntities.stream()
                .limit(HOT_SONG_LIMIT)
                .map(item -> toSongCard(item, categoryNameMap, singerAvatarMap))
                .toList();
        List<HomeSingerCardVO> hotSingers = singerEntities.stream()
                .map(this::toSingerCard)
                .toList();

        HomePageVO vo = new HomePageVO();
        vo.setBanners(resolveBanners(categoryNameMap, singerAvatarMap));
        vo.setRecommendedPlaylists(limitList(playlistCards, PLAYLIST_LIMIT));
        vo.setHotSongs(hotSongs);
        vo.setHotSingers(hotSingers);
        vo.setFeaturedSongs(resolveFeaturedSongs(songEntities, categoryNameMap, singerAvatarMap));
        vo.setTrends(List.of());
        vo.setCategories(buildCategoryCards(playlistEntities, categoryNameMap));
        vo.setPlayQueue(buildPlayQueue(vo, songMap, categoryNameMap, singerAvatarMap));
        return vo;
    }

    private Map<String, String> buildCategoryNameMap() {
        List<DictItemVO> items = dictService.listEnabledItems(null, "category");
        Map<String, String> categoryNameMap = new LinkedHashMap<>();
        for (DictItemVO item : items) {
            if (item.getId() != null) {
                categoryNameMap.put(String.valueOf(item.getId()), item.getName());
            }
        }
        return categoryNameMap;
    }

    private Map<Long, String> buildSingerAvatarMap(List<Singer> singers) {
        Map<Long, String> singerAvatarMap = new LinkedHashMap<>();
        for (Singer singer : singers) {
            if (singer.getId() != null) {
                singerAvatarMap.put(singer.getId(), singer.getAvatar());
            }
        }
        return singerAvatarMap;
    }

    private List<HomePlaylistCardVO> resolveBanners(Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
        return homeRecommendService.listEnabledByPosition(POSITION_HOME_BANNER).stream()
                .map(item -> toBannerCard(item, categoryNameMap, singerAvatarMap))
                .filter(Objects::nonNull)
                .limit(BANNER_LIMIT)
                .toList();
    }

    private List<HomeSongCardVO> resolveFeaturedSongs(List<Song> songs, Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
        List<HomeSongCardVO> featuredSongs = homeRecommendService.listEnabledByPosition(POSITION_HOME_DAILY_SONG).stream()
                .map(item -> toFeaturedSongCard(item, categoryNameMap, singerAvatarMap))
                .filter(Objects::nonNull)
                .limit(FEATURED_LIMIT)
                .toList();
        if (!featuredSongs.isEmpty()) {
            return featuredSongs;
        }

        return songs.stream()
                .filter(item -> StringUtils.hasText(item.getAudioUrl()))
                .limit(FEATURED_LIMIT)
                .map(item -> toSongCard(item, categoryNameMap, singerAvatarMap))
                .toList();
    }

    private HomeSongCardVO toFeaturedSongCard(
            HomeRecommendVO recommend,
            Map<String, String> categoryNameMap,
            Map<Long, String> singerAvatarMap
    ) {
        return switch (recommend.getTargetType()) {
            case TARGET_SONG -> buildFeaturedSongFromSong(recommend, categoryNameMap, singerAvatarMap);
            case TARGET_PLAYLIST -> buildFeaturedSongFromPlaylist(recommend, categoryNameMap, singerAvatarMap);
            case TARGET_SINGER -> buildFeaturedSongFromSinger(recommend, categoryNameMap, singerAvatarMap);
            default -> null;
        };
    }

    private HomeSongCardVO buildFeaturedSongFromSong(
            HomeRecommendVO recommend,
            Map<String, String> categoryNameMap,
            Map<Long, String> singerAvatarMap
    ) {
        Song song = songMapper.selectById(recommend.getTargetId());
        if (song == null) {
            return null;
        }
        HomeSongCardVO vo = toSongCard(song, categoryNameMap, singerAvatarMap);
        applyFeaturedRecommend(vo, recommend);
        return vo;
    }

    private HomeSongCardVO buildFeaturedSongFromPlaylist(
            HomeRecommendVO recommend,
            Map<String, String> categoryNameMap,
            Map<Long, String> singerAvatarMap
    ) {
        Playlist playlist = playlistMapper.selectById(recommend.getTargetId());
        if (playlist == null) {
            return null;
        }
        List<Long> songIds = playlistMapper.selectSongIdsByPlaylistId(playlist.getId());
        if (songIds.isEmpty()) {
            return null;
        }
        Song song = songMapper.selectById(songIds.get(0));
        if (song == null) {
            return null;
        }
        HomeSongCardVO vo = toSongCard(song, categoryNameMap, singerAvatarMap);
        vo.setTargetType(TARGET_PLAYLIST);
        vo.setTargetId(playlist.getId());
        vo.setName(resolveText(playlist.getName(), vo.getName()));
        vo.setDesc(buildPlaylistDesc(playlist));
        vo.setCover(resolveText(recommend.getCover(), firstNonBlank(playlist.getCover(), vo.getCover(), vo.getSingerAvatar())));
        return vo;
    }

    private HomeSongCardVO buildFeaturedSongFromSinger(
            HomeRecommendVO recommend,
            Map<String, String> categoryNameMap,
            Map<Long, String> singerAvatarMap
    ) {
        Singer singer = singerMapper.selectById(recommend.getTargetId());
        if (singer == null) {
            return null;
        }
        Song song = songMapper.selectFirstEnabledBySingerId(singer.getId());
        if (song == null) {
            return null;
        }
        HomeSongCardVO vo = toSongCard(song, categoryNameMap, singerAvatarMap);
        vo.setTargetType(TARGET_SINGER);
        vo.setTargetId(singer.getId());
        vo.setName(resolveText(singer.getName(), vo.getName()));
        vo.setDesc(buildSingerDesc(singer));
        vo.setCover(resolveText(recommend.getCover(), firstNonBlank(singer.getAvatar(), vo.getCover(), vo.getSingerAvatar())));
        return vo;
    }

    private void applyFeaturedRecommend(HomeSongCardVO vo, HomeRecommendVO recommend) {
        vo.setTargetType(recommend.getTargetType());
        vo.setTargetId(recommend.getTargetId());
        vo.setCover(resolveText(recommend.getCover(), firstNonBlank(vo.getCover(), vo.getSingerAvatar())));
    }

    private List<HomeCategoryCardVO> buildCategoryCards(List<Playlist> playlists, Map<String, String> categoryNameMap) {
        Map<String, Long> categoryCountMap = new LinkedHashMap<>();
        for (Playlist playlist : playlists) {
            if (!StringUtils.hasText(playlist.getCategory())) {
                continue;
            }
            categoryCountMap.merge(playlist.getCategory(), 1L, Long::sum);
        }

        List<HomeCategoryCardVO> list = new ArrayList<>();
        categoryNameMap.forEach((categoryId, categoryName) -> {
            Long count = categoryCountMap.get(categoryId);
            if (count == null || count < 1) {
                return;
            }
            HomeCategoryCardVO vo = new HomeCategoryCardVO();
            vo.setCategory(categoryId);
            vo.setTitle(categoryName);
            vo.setDesc("已收录 " + count + " 张精选歌单，适合按风格浏览和连续播放。");
            vo.setCountText(count + " 张精选歌单");
            list.add(vo);
        });

        return limitList(list, CATEGORY_CARD_LIMIT);
    }

    private List<HomeSongCardVO> buildPlayQueue(
            HomePageVO homePageVO,
            Map<Long, Song> songMap,
            Map<String, String> categoryNameMap,
            Map<Long, String> singerAvatarMap
    ) {
        LinkedHashSet<Long> songIds = new LinkedHashSet<>();
        homePageVO.getFeaturedSongs().forEach(item -> songIds.add(item.getId()));
        homePageVO.getHotSongs().forEach(item -> songIds.add(item.getId()));
        homePageVO.getBanners().forEach(item -> {
            if (item.getFirstSongId() != null) {
                songIds.add(item.getFirstSongId());
            }
        });
        homePageVO.getRecommendedPlaylists().forEach(item -> {
            if (item.getFirstSongId() != null) {
                songIds.add(item.getFirstSongId());
            }
        });

        List<HomeSongCardVO> playQueue = new ArrayList<>();
        for (Long songId : songIds) {
            Song song = songMap.get(songId);
            if (song == null && songId != null) {
                song = songMapper.selectById(songId);
            }
            if (song != null) {
                playQueue.add(toSongCard(song, categoryNameMap, singerAvatarMap));
            }
        }
        return playQueue;
    }

    private HomePlaylistCardVO toBannerCard(HomeRecommendVO recommend, Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
        return switch (recommend.getTargetType()) {
            case TARGET_PLAYLIST -> buildPlaylistBanner(recommend, categoryNameMap);
            case TARGET_SONG -> buildSongBanner(recommend, categoryNameMap, singerAvatarMap);
            case TARGET_SINGER -> buildSingerBanner(recommend);
            default -> null;
        };
    }

    private HomePlaylistCardVO buildPlaylistBanner(HomeRecommendVO recommend, Map<String, String> categoryNameMap) {
        Playlist playlist = playlistMapper.selectById(recommend.getTargetId());
        if (playlist == null) {
            return null;
        }
        HomePlaylistCardVO vo = toPlaylistCard(playlist, categoryNameMap);
        vo.setTargetType(TARGET_PLAYLIST);
        vo.setTargetId(playlist.getId());
        vo.setCover(resolveText(recommend.getCover(), vo.getCover()));
        vo.setTag("首页推荐");
        return vo;
    }

    private HomePlaylistCardVO buildSongBanner(HomeRecommendVO recommend, Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
        Song song = songMapper.selectById(recommend.getTargetId());
        if (song == null) {
            return null;
        }
        HomeSongCardVO songCard = toSongCard(song, categoryNameMap, singerAvatarMap);
        HomePlaylistCardVO vo = new HomePlaylistCardVO();
        vo.setId(song.getId());
        vo.setTargetType(TARGET_SONG);
        vo.setTargetId(song.getId());
        vo.setTag("单曲精选");
        vo.setTitle(song.getName());
        vo.setDesc(songCard.getDesc());
        vo.setMeta(songCard.getMeta());
        vo.setCover(resolveText(recommend.getCover(), firstNonBlank(song.getCover(), songCard.getSingerAvatar())));
        vo.setCategory(song.getCategory());
        vo.setCategoryName(songCard.getMeta());
        vo.setSongIds(List.of(song.getId()));
        vo.setFirstSongId(song.getId());
        return vo;
    }

    private HomePlaylistCardVO buildSingerBanner(HomeRecommendVO recommend) {
        Singer singer = singerMapper.selectById(recommend.getTargetId());
        if (singer == null) {
            return null;
        }
        Song firstSong = songMapper.selectFirstEnabledBySingerId(singer.getId());
        HomePlaylistCardVO vo = new HomePlaylistCardVO();
        vo.setId(singer.getId());
        vo.setTargetType(TARGET_SINGER);
        vo.setTargetId(singer.getId());
        vo.setTag("歌手推荐");
        vo.setTitle(singer.getName());
        vo.setDesc(buildSingerDesc(singer));
        vo.setMeta(StringUtils.hasText(singer.getRegion()) ? singer.getRegion().trim() : "热门歌手");
        vo.setCover(resolveText(recommend.getCover(), singer.getAvatar()));
        vo.setSongIds(firstSong == null ? List.of() : List.of(firstSong.getId()));
        vo.setFirstSongId(firstSong == null ? null : firstSong.getId());
        return vo;
    }

    private HomePlaylistCardVO toPlaylistCard(Playlist entity, Map<String, String> categoryNameMap) {
        HomePlaylistCardVO vo = new HomePlaylistCardVO();
        vo.setId(entity.getId());
        vo.setTargetType(TARGET_PLAYLIST);
        vo.setTargetId(entity.getId());
        vo.setTitle(entity.getName());
        vo.setDesc(buildPlaylistDesc(entity));
        vo.setMeta((entity.getSongCount() == null ? 0 : entity.getSongCount()) + " 首 · 收藏 " + formatCount(entity.getFavoriteCount()));
        vo.setCover(entity.getCover());
        vo.setCategory(entity.getCategory());
        vo.setCategoryName(resolveCategoryName(entity.getCategory(), categoryNameMap));
        vo.setTag(resolvePlaylistTag(entity, vo.getCategoryName()));
        List<Long> songIds = playlistMapper.selectSongIdsByPlaylistId(entity.getId());
        vo.setSongIds(songIds);
        vo.setFirstSongId(songIds.isEmpty() ? null : songIds.get(0));
        return vo;
    }

    private HomeSongCardVO toSongCard(Song entity, Map<String, String> categoryNameMap, Map<Long, String> singerAvatarMap) {
        HomeSongCardVO vo = new HomeSongCardVO();
        vo.setId(entity.getId());
        vo.setTargetType(TARGET_SONG);
        vo.setTargetId(entity.getId());
        vo.setName(entity.getName());
        vo.setSinger(entity.getSingerName());
        vo.setDesc(buildSongDesc(entity));
        vo.setMeta(resolveCategoryName(entity.getCategory(), categoryNameMap));
        vo.setTime(formatDuration(entity.getDurationSeconds()));
        vo.setDurationSeconds(entity.getDurationSeconds());
        vo.setPlaylistId(null);
        vo.setCover(entity.getCover());
        vo.setSingerAvatar(resolveSingerAvatar(entity.getSingerId(), singerAvatarMap));
        vo.setAudioUrl(entity.getAudioUrl());
        return vo;
    }

    private String resolveSingerAvatar(Long singerId, Map<Long, String> singerAvatarMap) {
        if (singerId == null) {
            return null;
        }
        if (singerAvatarMap.containsKey(singerId)) {
            return singerAvatarMap.get(singerId);
        }
        Singer singer = singerMapper.selectById(singerId);
        String avatar = singer == null ? null : singer.getAvatar();
        singerAvatarMap.put(singerId, avatar);
        return avatar;
    }

    private HomeSingerCardVO toSingerCard(Singer entity) {
        HomeSingerCardVO vo = new HomeSingerCardVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDesc(buildSingerDesc(entity));
        vo.setCount("最近更新 · " + formatDateText(entity.getUpdateTime() == null ? null : entity.getUpdateTime().toLocalDate()));
        vo.setAvatar(entity.getAvatar());
        return vo;
    }

    private String resolvePlaylistTag(Playlist entity, String categoryName) {
        if (StringUtils.hasText(categoryName)) {
            return categoryName;
        }
        if (StringUtils.hasText(entity.getTags())) {
            return entity.getTags().split(",")[0].trim();
        }
        return "精选歌单";
    }

    private String resolveCategoryName(String category, Map<String, String> categoryNameMap) {
        if (!StringUtils.hasText(category)) {
            return "未分类";
        }
        return categoryNameMap.getOrDefault(category, category);
    }

    private String buildPlaylistDesc(Playlist entity) {
        if (StringUtils.hasText(entity.getIntro())) {
            return entity.getIntro().trim();
        }
        if (StringUtils.hasText(entity.getSubtitle())) {
            return entity.getSubtitle().trim();
        }
        return "适合连续播放的精选歌单。";
    }

    private String buildSongDesc(Song entity) {
        if (StringUtils.hasText(entity.getSubtitle())) {
            return entity.getSubtitle().trim();
        }
        if (StringUtils.hasText(entity.getLanguage())) {
            return entity.getLanguage().trim() + " · " + entity.getSingerName();
        }
        return entity.getSingerName();
    }

    private String buildSingerDesc(Singer entity) {
        if (StringUtils.hasText(entity.getIntro())) {
            return entity.getIntro().trim();
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(entity.getRegion())) {
            parts.add(entity.getRegion().trim());
        }
        if (StringUtils.hasText(entity.getTags())) {
            parts.add(entity.getTags().split(",")[0].trim());
        }
        return parts.isEmpty() ? "风格鲜明的热门歌手" : String.join(" · ", parts);
    }

    private String formatDuration(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds < 1) {
            return "--:--";
        }
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private String formatCount(Long count) {
        long safeCount = count == null ? 0L : count;
        if (safeCount >= 10000) {
            double value = safeCount / 10000.0;
            return String.format(Locale.ROOT, "%.1f万", value);
        }
        return String.valueOf(safeCount);
    }

    private String formatDateText(java.time.LocalDate date) {
        if (date == null) {
            return "近期";
        }
        return date.toString();
    }

    private String resolveText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private <T> List<T> limitList(List<T> source, int limit) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream().filter(Objects::nonNull).limit(limit).toList();
    }
}
