package com.label.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.label.admin.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    SysFile uploadFile(MultipartFile file, Long uploaderId);

    void deleteFile(Long id, Long currentUserId, String roleCode);

    SysFile getFileById(Long id);

    IPage<SysFile> getFilePage(int current, int size, String originalName, Long uploaderId);

    byte[] downloadFile(Long id);

    long countFiles();
}
