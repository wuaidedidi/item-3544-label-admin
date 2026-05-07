package com.label.admin.util;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class FileExcelData {

    @ExcelProperty("文件名")
    private String originalName;

    @ExcelProperty("文件类型")
    private String fileType;

    @ExcelProperty("文件大小")
    private String fileSize;

    @ExcelProperty("上传者")
    private String uploaderName;

    @ExcelProperty("上传时间")
    private String createdAt;
}
