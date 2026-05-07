package com.label.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.label.admin.entity.SysFile;
import com.label.admin.exception.BusinessException;
import com.label.admin.mapper.SysFileMapper;
import com.label.admin.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${app.upload.path}")
    private String uploadPath;

    private final SysFileMapper fileMapper;

    public FileServiceImpl(SysFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public SysFile uploadFile(MultipartFile file, Long uploaderId) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path filePath = uploadDir.resolve(storedName);
            Files.write(filePath, file.getBytes());

            SysFile sysFile = new SysFile();
            sysFile.setOriginalName(originalName);
            sysFile.setStoredName(storedName);
            sysFile.setFilePath(filePath.toString());
            sysFile.setFileSize(file.getSize());
            sysFile.setFileType(file.getContentType());
            sysFile.setUploaderId(uploaderId);
            fileMapper.insert(sysFile);

            log.info("文件上传成功: {} -> {}", originalName, storedName);
            return sysFile;
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(Long id, Long currentUserId, String roleCode) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        if (!"ADMIN".equals(roleCode) && !sysFile.getUploaderId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己上传的文件");
        }

        try {
            Path filePath = Paths.get(sysFile.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", e.getMessage());
        }

        fileMapper.deleteById(id);
        log.info("文件删除成功: id={}", id);
    }

    @Override
    public SysFile getFileById(Long id) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        return sysFile;
    }

    @Override
    public IPage<SysFile> getFilePage(int current, int size, String originalName, Long uploaderId) {
        Page<SysFile> page = new Page<>(current, size);
        return fileMapper.selectFilePage(page, originalName, uploaderId);
    }

    @Override
    public byte[] downloadFile(Long id) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        try {
            Path filePath = Paths.get(sysFile.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BusinessException("文件不存在或已被删除");
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new BusinessException("文件下载失败");
        }
    }

    @Override
    public long countFiles() {
        return fileMapper.selectCount(null);
    }
}
