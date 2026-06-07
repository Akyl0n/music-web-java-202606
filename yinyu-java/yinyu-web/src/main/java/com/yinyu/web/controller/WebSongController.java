package com.yinyu.web.controller;

import com.yinyu.api.ApiResponse;
import com.yinyu.api.ListData;
import com.yinyu.entity.dto.SongQueryRequest;
import com.yinyu.entity.dto.UserSongActionRequest;
import com.yinyu.entity.po.Song;
import com.yinyu.entity.vo.SongVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.mapper.SongMapper;
import com.yinyu.service.FileStorageService;
import com.yinyu.service.SongService;
import com.yinyu.service.UserService;
import com.yinyu.web.support.WebSongMediaSupport;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/web/songs")
public class WebSongController {

    private static final String PLAY_RECORD_PREFIX = "web:song:media:last-play:";
    private static final long PLAY_RECORD_INTERVAL_MILLIS = 15000L;

    private final SongMapper songMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final SongService songService;
    private final WebSongMediaSupport webSongMediaSupport;

    public WebSongController(SongMapper songMapper, FileStorageService fileStorageService, UserService userService, SongService songService, WebSongMediaSupport webSongMediaSupport) {
        this.songMapper = songMapper;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.songService = songService;
        this.webSongMediaSupport = webSongMediaSupport;
    }

    @GetMapping
    public ApiResponse<ListData<SongVO>> list(SongQueryRequest request) {
        SongQueryRequest query = request == null ? new SongQueryRequest() : request;
        query.setStatus("enabled");
        ListData<SongVO> data = songService.listPage(query);
        webSongMediaSupport.apply(data.getList());
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<?> media(
        @PathVariable Long id,
        @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
        HttpSession session
    ) throws IOException {
        Song song = requirePlayableSong(id);
        recordPlayIfNeeded(song, range, session);

        FileLocation location = parseAudioUrl(song.getAudioUrl());
        Resource resource = fileStorageService.loadAsResource(location.category(), location.monthFolder(), location.fileName());
        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        long contentLength = resource.contentLength();

        if (range == null || range.isBlank()) {
            return ResponseEntity.ok()
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(mediaType)
                .contentLength(contentLength)
                .body(resource);
        }

        long start = parseRangeStart(range, contentLength);
        long end = parseRangeEnd(range, contentLength);
        long rangeLength = end - start + 1;

        InputStream inputStream = resource.getInputStream();
        inputStream.skipNBytes(start);
        InputStreamResource body = new InputStreamResource(new RangeInputStream(inputStream, rangeLength));

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
            .contentType(mediaType)
            .contentLength(rangeLength)
            .body(body);
    }

    private Song requirePlayableSong(Long id) {
        Song song = songMapper.selectById(id);
        if (song == null || !"enabled".equalsIgnoreCase(song.getStatus())) {
            throw new BusinessException("song not found");
        }
        if (song.getAudioUrl() == null || song.getAudioUrl().isBlank()) {
            throw new BusinessException("song audio not found");
        }
        return song;
    }

    private void recordPlayIfNeeded(Song song, String range, HttpSession session) {
        if (!shouldRecordPlay(song.getId(), range, session)) {
            return;
        }
        if (userService.getCurrentUser(session) == null) {
            return;
        }
        UserSongActionRequest request = new UserSongActionRequest();
        request.setSongId(song.getId());
        request.setProgressSeconds(0);
        userService.recordPlaySong(request, session);
    }

    private boolean shouldRecordPlay(Long songId, String range, HttpSession session) {
        if (songId == null) {
            return false;
        }
        if (range != null && !range.isBlank()) {
            long start = parseRangeStart(range, Long.MAX_VALUE);
            if (start > 0) {
                return false;
            }
        }
        String sessionKey = PLAY_RECORD_PREFIX + songId;
        Object cachedValue = session.getAttribute(sessionKey);
        long currentTime = System.currentTimeMillis();
        if (cachedValue instanceof Long lastTime && currentTime - lastTime < PLAY_RECORD_INTERVAL_MILLIS) {
            return false;
        }
        session.setAttribute(sessionKey, currentTime);
        return true;
    }

    private FileLocation parseAudioUrl(String audioUrl) {
        if (audioUrl == null || audioUrl.isBlank()) {
            throw new BusinessException("song audio not found");
        }
        String normalized = audioUrl.replace("\\", "/").trim();
        String prefix = "/api/files/";
        int startIndex = normalized.indexOf(prefix);
        if (startIndex >= 0) {
            normalized = normalized.substring(startIndex + prefix.length());
        } else if (normalized.startsWith("/files/")) {
            normalized = normalized.substring("/files/".length());
        } else if (normalized.startsWith("files/")) {
            normalized = normalized.substring("files/".length());
        }
        String[] parts = normalized.split("/");
        if (parts.length < 3) {
            throw new BusinessException("song audio path is invalid");
        }
        return new FileLocation(parts[0], parts[1], parts[2]);
    }

    private long parseRangeStart(String range, long contentLength) {
        String value = range.replace("bytes=", "").trim();
        String[] parts = value.split("-", 2);
        if (parts.length == 0 || parts[0].isBlank()) {
            return 0L;
        }
        long start = Long.parseLong(parts[0].trim());
        if (contentLength == Long.MAX_VALUE) {
            return Math.max(0L, start);
        }
        return Math.max(0L, Math.min(start, Math.max(0L, contentLength - 1)));
    }

    private long parseRangeEnd(String range, long contentLength) {
        String value = range.replace("bytes=", "").trim();
        String[] parts = value.split("-", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            return Math.max(0L, contentLength - 1);
        }
        long end = Long.parseLong(parts[1].trim());
        long maxEnd = Math.max(0L, contentLength - 1);
        return Math.max(0L, Math.min(end, maxEnd));
    }

    private record FileLocation(String category, String monthFolder, String fileName) {
    }

    private static class RangeInputStream extends FilterInputStream {

        private long remaining;

        protected RangeInputStream(InputStream in, long remaining) {
            super(in);
            this.remaining = remaining;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int value = super.read();
            if (value != -1) {
                remaining--;
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int maxLen = (int) Math.min(len, remaining);
            int count = super.read(b, off, maxLen);
            if (count > 0) {
                remaining -= count;
            }
            return count;
        }
    }
}
