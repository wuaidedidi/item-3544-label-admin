package com.label.admin.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.label.admin.common.PageResult;
import com.label.admin.common.Result;
import com.label.admin.entity.SysFile;
import com.label.admin.security.JwtUserDetails;
import com.label.admin.service.FileService;
import com.label.admin.util.FileExcelData;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.label.admin.annotation.OperationLog;
import com.label.admin.annotation.RequiresPermission;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public Result<PageResult<SysFile>> getFileList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String originalName,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long uploaderId = "ADMIN".equals(userDetails.getRoleCode()) ? null : userDetails.getUserId();
        IPage<SysFile> page = fileService.getFilePage(current, size, originalName, uploaderId);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/upload")
    @RequiresPermission("文件上传")
    @OperationLog("上传文件")
    public Result<SysFile> uploadFile(@RequestParam("file") MultipartFile file,
                                      @AuthenticationPrincipal JwtUserDetails userDetails) {
        SysFile sysFile = fileService.uploadFile(file, userDetails.getUserId());
        return Result.success("上传成功", sysFile);
    }

    @PostMapping("/upload/batch")
    @OperationLog("批量上传文件")
    public Result<String> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                      @AuthenticationPrincipal JwtUserDetails userDetails) {
        int successCount = 0;
        for (MultipartFile file : files) {
            try {
                fileService.uploadFile(file, userDetails.getUserId());
                successCount++;
            } catch (Exception e) {
                log.warn("批量上传文件失败: {}", e.getMessage());
            }
        }
        return Result.success("成功上传 " + successCount + " 个文件", null);
    }

    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        SysFile sysFile = fileService.getFileById(id);
        byte[] data = fileService.downloadFile(id);

        response.setContentType(sysFile.getFileType());
        String fileName = URLEncoder.encode(sysFile.getOriginalName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @PostMapping("/download/batch")
    @OperationLog("批量下载文件")
    public void batchDownload(@RequestBody List<Long> ids, HttpServletResponse response) throws IOException {
        if (ids == null || ids.isEmpty()) {
            response.setStatus(400);
            return;
        }
        response.setContentType("application/zip");
        String fileName = URLEncoder.encode("批量下载文件", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : ids) {
                try {
                    SysFile sysFile = fileService.getFileById(id);
                    byte[] data = fileService.downloadFile(id);
                    zos.putNextEntry(new ZipEntry(sysFile.getOriginalName()));
                    zos.write(data);
                    zos.closeEntry();
                } catch (Exception e) {
                    log.warn("批量下载跳过文件ID={}: {}", id, e.getMessage());
                }
            }
        }
    }

    @GetMapping("/export")
    @OperationLog("导出文件列表")
    public void exportFiles(
            @RequestParam(required = false) String originalName,
            @AuthenticationPrincipal JwtUserDetails userDetails,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("文件列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        Long uploaderId = "ADMIN".equals(userDetails.getRoleCode()) ? null : userDetails.getUserId();
        IPage<SysFile> page = fileService.getFilePage(1, 10000, originalName, uploaderId);
        List<FileExcelData> dataList = new ArrayList<>();
        for (SysFile file : page.getRecords()) {
            FileExcelData data = new FileExcelData();
            data.setOriginalName(file.getOriginalName());
            data.setFileType(file.getFileType());
            data.setFileSize(formatFileSize(file.getFileSize()));
            data.setUploaderName(file.getUploaderName() != null ? file.getUploaderName() : "");
            data.setCreatedAt(file.getCreatedAt() != null ? file.getCreatedAt().toString() : "");
            dataList.add(data);
        }

        EasyExcel.write(response.getOutputStream(), FileExcelData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("文件列表")
                .doWrite(dataList);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("文件删除")
    @OperationLog("删除文件")
    public Result<Void> deleteFile(@PathVariable Long id,
                                   @AuthenticationPrincipal JwtUserDetails userDetails) {
        fileService.deleteFile(id, userDetails.getUserId(), userDetails.getRoleCode());
        return Result.success("删除成功", null);
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int i = (int) (Math.log(bytes) / Math.log(1024));
        if (i >= units.length) i = units.length - 1;
        return String.format("%.2f %s", bytes / Math.pow(1024, i), units[i]);
    }
}
