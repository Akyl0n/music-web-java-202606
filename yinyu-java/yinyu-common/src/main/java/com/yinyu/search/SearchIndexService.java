package com.yinyu.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yinyu.entity.dto.PlaylistQueryRequest;
import com.yinyu.entity.dto.SingerQueryRequest;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.po.Playlist;
import com.yinyu.entity.po.Singer;
import com.yinyu.entity.po.Song;
import com.yinyu.mapper.PlaylistMapper;
import com.yinyu.mapper.SingerMapper;
import com.yinyu.mapper.SongMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchIndexService {

    public static final String TYPE_SONG = "song";
    public static final String TYPE_PLAYLIST = "playlist";
    public static final String TYPE_SINGER = "singer";

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchIndexService.class);

    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;
    private final SongMapper songMapper;
    private final PlaylistMapper playlistMapper;
    private final SingerMapper singerMapper;

    public SearchIndexService(
            ElasticsearchOperations elasticsearchOperations,
            ObjectMapper objectMapper,
            SongMapper songMapper,
            PlaylistMapper playlistMapper,
            SingerMapper singerMapper
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.objectMapper = objectMapper;
        this.songMapper = songMapper;
        this.playlistMapper = playlistMapper;
        this.singerMapper = singerMapper;
    }

    public SearchPage searchSongs(SongQueryRequest query) {
        if (query == null || !StringUtils.hasText(query.getKeyword())) {
            return null;
        }
        List<Map<String, Object>> filters = new ArrayList<>();
        addTermFilter(filters, "contentType", TYPE_SONG);
        addTermFilter(filters, "singerId", query.getSingerId());
        addTermFilter(filters, "category", query.getCategory());
        addTermFilter(filters, "status", query.getStatus());
        return search(query.getKeyword(), filters, query.getOffset(), query.getLimit());
    }

    public SearchPage searchPlaylists(PlaylistQueryRequest query) {
        if (query == null || !StringUtils.hasText(query.getKeyword())) {
            return null;
        }
        List<Map<String, Object>> filters = new ArrayList<>();
        addTermFilter(filters, "contentType", TYPE_PLAYLIST);
        addTermFilter(filters, "category", query.getCategory());
        addTermFilter(filters, "status", query.getStatus());
        return search(query.getKeyword(), filters, query.getOffset(), query.getLimit());
    }

    public SearchPage searchSingers(SingerQueryRequest query) {
        if (query == null || !StringUtils.hasText(query.getKeyword())) {
            return null;
        }
        List<Map<String, Object>> filters = new ArrayList<>();
        addTermFilter(filters, "contentType", TYPE_SINGER);
        addTermFilter(filters, "status", query.getStatus());
        addTermFilter(filters, "gender", query.getGender());
        addTermFilter(filters, "region", query.getRegion());
        addTermFilter(filters, "letter", query.getLetter());
        addTermFilter(filters, "tagValues", query.getTag());
        addTermFilter(filters, "type", query.getType());
        return search(query.getKeyword(), filters, query.getOffset(), query.getLimit());
    }

    public void syncContent(String contentType, Long contentId) {
        try {
            switch (contentType) {
                case TYPE_SONG -> syncSong(contentId);
                case TYPE_PLAYLIST -> syncPlaylist(contentId);
                case TYPE_SINGER -> syncSinger(contentId);
                default -> LOGGER.warn("Unsupported search index content type: {}", contentType);
            }
        } catch (Exception ex) {
            LOGGER.warn("Search index sync failed, contentType={}, contentId={}", contentType, contentId, ex);
        }
    }

    public void deleteContent(String contentType, Long contentId) {
        try {
            elasticsearchOperations.delete(SearchDocument.buildId(contentType, contentId), SearchDocument.class);
        } catch (Exception ex) {
            LOGGER.warn("Search index delete failed, contentType={}, contentId={}", contentType, contentId, ex);
        }
    }

    private SearchPage search(String keyword, List<Map<String, Object>> filters, int offset, int limit) {
        try {
            StringQuery query = new StringQuery(buildQuery(keyword, filters));
            int page = limit < 1 ? 0 : offset / limit;
            int size = limit < 1 ? 10 : limit;
            query.setPageable(PageRequest.of(page, size));
            SearchHits<SearchDocument> hits = elasticsearchOperations.search(query, SearchDocument.class);
            List<Long> ids = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(SearchDocument::getContentId)
                    .toList();
            return new SearchPage(ids, hits.getTotalHits());
        } catch (Exception ex) {
            LOGGER.warn("Elasticsearch search failed, fallback to database, keyword={}", keyword, ex);
            return null;
        }
    }

    private String buildQuery(String keyword, List<Map<String, Object>> filters) throws com.fasterxml.jackson.core.JsonProcessingException {
        Map<String, Object> query = new LinkedHashMap<>();
        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("filter", filters);
        bool.put("must", List.of(Map.of("multi_match", Map.of(
                "query", keyword.trim(),
                "fields", List.of("name^4", "subtitle^2", "description", "tags", "category"),
                "type", "best_fields"
        ))));
        query.put("query", Map.of("bool", bool));
        query.put("sort", List.of(
                Map.of("_score", Map.of("order", "desc")),
                Map.of("sortValue", Map.of("order", "desc", "missing", "_last"))
        ));
        return objectMapper.writeValueAsString(query);
    }

    private void addTermFilter(List<Map<String, Object>> filters, String field, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return;
        }
        filters.add(Map.of("term", Map.of(field, value instanceof String text ? text.trim() : value)));
    }

    private void syncSong(Long id) {
        Song song = songMapper.selectById(id);
        if (song == null) {
            deleteContent(TYPE_SONG, id);
            return;
        }
        SearchDocument document = new SearchDocument();
        document.setId(SearchDocument.buildId(TYPE_SONG, song.getId()));
        document.setContentType(TYPE_SONG);
        document.setContentId(song.getId());
        document.setName(song.getName());
        document.setSubtitle(song.getSubtitle());
        document.setDescription(song.getIntro());
        document.setTags(joinText(song.getSingerName(), song.getTags(), song.getLanguage()));
        document.setTagValues(splitTags(song.getTags()));
        document.setCategory(song.getCategory());
        document.setSingerId(song.getSingerId());
        document.setStatus(song.getStatus());
        document.setSortValue(defaultLong(song.getPlayCount()) + defaultLong(song.getFavoriteCount()) + defaultLong(song.getLikeCount()));
        elasticsearchOperations.save(document);
    }

    private void syncPlaylist(Long id) {
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) {
            deleteContent(TYPE_PLAYLIST, id);
            return;
        }
        SearchDocument document = new SearchDocument();
        document.setId(SearchDocument.buildId(TYPE_PLAYLIST, playlist.getId()));
        document.setContentType(TYPE_PLAYLIST);
        document.setContentId(playlist.getId());
        document.setName(playlist.getName());
        document.setSubtitle(playlist.getSubtitle());
        document.setDescription(playlist.getIntro());
        document.setTags(playlist.getTags());
        document.setTagValues(splitTags(playlist.getTags()));
        document.setCategory(playlist.getCategory());
        document.setStatus(playlist.getStatus());
        document.setSortValue(defaultLong(playlist.getPlayCount()) + defaultLong(playlist.getFavoriteCount()));
        elasticsearchOperations.save(document);
    }

    private void syncSinger(Long id) {
        Singer singer = singerMapper.selectById(id);
        if (singer == null) {
            deleteContent(TYPE_SINGER, id);
            return;
        }
        SearchDocument document = new SearchDocument();
        document.setId(SearchDocument.buildId(TYPE_SINGER, singer.getId()));
        document.setContentType(TYPE_SINGER);
        document.setContentId(singer.getId());
        document.setName(singer.getName());
        document.setDescription(singer.getIntro());
        document.setTags(joinText(singer.getTags(), singer.getRegion(), singer.getType(), singer.getLetter()));
        document.setTagValues(splitTags(singer.getTags()));
        document.setGender(singer.getGender());
        document.setRegion(singer.getRegion());
        document.setLetter(singer.getLetter());
        document.setType(singer.getType());
        document.setStatus(singer.getStatus());
        document.setSortValue(singer.getId());
        elasticsearchOperations.save(document);
    }

    private String joinText(String... values) {
        List<String> parts = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (StringUtils.hasText(value)) {
                    parts.add(value.trim());
                }
            }
        }
        return String.join(" ", parts);
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        return List.of(tags.replace('，', ',').split(","))
                .stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
