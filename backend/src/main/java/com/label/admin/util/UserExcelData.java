package com.label.admin.util;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserExcelData {

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("角色")
    private String roleName;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("创建时间")
    private String createdAt;
}
