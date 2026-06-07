package com.yinyu.service.impl;

import com.yinyu.entity.vo.FileUploadVO;
import com.yinyu.exception.BusinessException;
import com.yinyu.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of(".mp3", ".wav", ".flac", ".m4a", ".aac", ".ogg");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final Path uploadRoot;

    public FileStorageServiceImpl(@Value("${project.folder}") String projectFolder) {
        this.uploadRoot = Paths.get(projectFolder).toAbsolutePath().normalize().resolve("uploads");
    }

    @Override
    public FileUploadVO storeImage(MultipartFile file, String category) {
        validateFileNotEmpty(file);
        String extension = resolveExtension(file.getOriginalFilename());
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException("only image files are supported");
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("image extension is not supported");
        }
        return storeFile(file, category, extension);
    }

    @Override
    public FileUploadVO storeAudio(MultipartFile file, String category) {
        validateFileNotEmpty(file);
        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension)) {
            throw new BusinessException("audio extension is not supported");
        }
        return storeFile(file, category, extension);
    }

    private FileUploadVO storeFile(MultipartFile file, String category, String extension) {
        String safeCategory = safePathSegment(category, "category is invalid");
        String monthFolder = LocalDate.now().format(MONTH_FORMATTER);
        String targetFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetDirectory = uploadRoot.resolve(safeCategory).resolve(monthFolder).normalize();
        Path targetFile = targetDirectory.resolve(targetFileName).normalize();
        ensureInsideRoot(targetDirectory);
        ensureInsideRoot(targetFile);
        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException("store file failed");
        }
        return new FileUploadVO(targetFileName, "/api/files/" + safeCategory + "/" + monthFolder + "/" + targetFileName);
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("upload file can not be empty");
        }
    }

    @Override
    public Resource loadAsResource(String category, String monthFolder, String fileName) {
        String safeCategory = safePathSegment(category, "category is invalid");
        String safeMonthFolder = safePathSegment(monthFolder, "month folder is invalid");
        String safeFileName = safePathSegment(fileName, "file name is invalid");
        Path filePath = uploadRoot.resolve(safeCategory).resolve(safeMonthFolder).resolve(safeFileName).normalize();
        ensureInsideRoot(filePath);
        if (!Files.exists(filePath)) {
            throw new BusinessException("file not found");
        }
        try {
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException exception) {
            throw new BusinessException("file path is invalid");
        }
    }

    private String resolveExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException("file extension is missing");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("file extension is missing");
        }
        return extension;
    }

    private String safePathSegment(String value, String message) {
        if (!StringUtils.hasText(value) || value.contains("..") || value.contains("/") || value.contains("\\")) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private void ensureInsideRoot(Path path) {
        if (!path.startsWith(uploadRoot)) {
            throw new BusinessException("file path is invalid");
        }
    }
}
