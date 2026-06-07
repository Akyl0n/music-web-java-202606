package com.yinyu.admin.controller;

import com.yinyu.service.FileStorageService;
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
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{category}/{monthFolder}/{fileName:.+}")
    public ResponseEntity<?> viewFile(
        @PathVariable String category,
        @PathVariable String monthFolder,
        @PathVariable String fileName,
        @RequestHeader(value = HttpHeaders.RANGE, required = false) String range
    ) throws IOException {
        Resource resource = fileStorageService.loadAsResource(category, monthFolder, fileName);
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

    private long parseRangeStart(String range, long contentLength) {
        String value = range.replace("bytes=", "").trim();
        String[] parts = value.split("-", 2);
        if (parts.length == 0 || parts[0].isBlank()) {
            return 0L;
        }
        long start = Long.parseLong(parts[0].trim());
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
