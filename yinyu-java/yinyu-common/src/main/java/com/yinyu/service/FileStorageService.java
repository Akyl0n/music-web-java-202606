package com.yinyu.service;

import com.yinyu.entity.vo.FileUploadVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadVO storeImage(MultipartFile file, String category);

    FileUploadVO storeAudio(MultipartFile file, String category);

    Resource loadAsResource(String category, String monthFolder, String fileName);
}
