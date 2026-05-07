package com.label.admin.util;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RoleExcelData {

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("角色编码")
    private String roleCode;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("创建时间")
    private String createdAt;
}
