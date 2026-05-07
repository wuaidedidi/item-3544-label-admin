package com.label.admin.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.label.admin.common.PageResult;
import com.label.admin.common.Result;
import com.label.admin.dto.UserCreateDTO;
import com.label.admin.dto.UserUpdateDTO;
import com.label.admin.entity.SysUser;
import com.label.admin.security.JwtUserDetails;
import com.label.admin.service.UserService;
import com.label.admin.util.UserExcelData;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SysUser>> getUserList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        IPage<SysUser> page = userService.getUserPage(current, size, username, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("用户新增")
    @OperationLog("新增用户")
    public Result<Void> createUser(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return Result.success("创建用户成功", null);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("用户编辑")
    @OperationLog("编辑用户")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateDTO dto,
                                   @AuthenticationPrincipal JwtUserDetails userDetails) {
        userService.updateUser(dto, userDetails.getUserId());
        return Result.success("更新用户成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("用户删除")
    @OperationLog("删除用户")
    public Result<Void> deleteUser(@PathVariable Long id,
                                   @AuthenticationPrincipal JwtUserDetails userDetails) {
        userService.deleteUser(id, userDetails.getUserId());
        return Result.success("删除用户成功", null);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("用户导出")
    @OperationLog("导出用户列表")
    public void exportUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        IPage<SysUser> page = userService.getUserPage(1, 10000, username, status);
        List<UserExcelData> dataList = new ArrayList<>();
        for (SysUser user : page.getRecords()) {
            UserExcelData data = new UserExcelData();
            data.setUsername(user.getUsername());
            data.setNickname(user.getNickname());
            data.setEmail(user.getEmail());
            data.setPhone(user.getPhone());
            data.setRoleName(user.getRoleName());
            data.setStatus(user.getStatus() == 1 ? "启用" : "禁用");
            data.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            dataList.add(data);
        }

        EasyExcel.write(response.getOutputStream(), UserExcelData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("用户列表")
                .doWrite(dataList);

        log.info("导出用户列表成功，共{}条", dataList.size());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("用户导入")
    @OperationLog("导入用户")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            List<UserExcelData> dataList = EasyExcel.read(file.getInputStream())
                    .head(UserExcelData.class)
                    .sheet()
                    .doReadSync();

            int successCount = 0;
            int failCount = 0;
            StringBuilder errors = new StringBuilder();

            for (UserExcelData data : dataList) {
                try {
                    if (data.getUsername() == null || data.getUsername().trim().isEmpty()) {
                        failCount++;
                        errors.append("用户名不能为空; ");
                        continue;
                    }

                    SysUser existing = userService.getUserByUsername(data.getUsername().trim());
                    if (existing != null) {
                        failCount++;
                        errors.append("用户名 ").append(data.getUsername()).append(" 已存在; ");
                        continue;
                    }

                    UserCreateDTO dto = new UserCreateDTO();
                    dto.setUsername(data.getUsername().trim());
                    dto.setPassword("123456");
                    dto.setNickname(data.getNickname() != null ? data.getNickname().trim() : data.getUsername().trim());
                    dto.setEmail(data.getEmail() != null ? data.getEmail().trim() : "");
                    dto.setPhone(data.getPhone() != null ? data.getPhone().trim() : "");
                    dto.setRoleId(2L);
                    dto.setStatus(1);
                    userService.createUser(dto);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.append(data.getUsername()).append(": ").append(e.getMessage()).append("; ");
                }
            }

            String message = "导入完成：成功 " + successCount + " 条，失败 " + failCount + " 条";
            if (errors.length() > 0) {
                message += "。" + errors.toString();
            }

            log.info("导入用户: 成功{}条, 失败{}条", successCount, failCount);
            return Result.success(message, null);
        } catch (Exception e) {
            log.error("导入用户失败: {}", e.getMessage());
            return Result.error("导入失败：" + e.getMessage());
        }
    }
}
