package com.label.admin.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.label.admin.common.PageResult;
import com.label.admin.common.Result;
import com.label.admin.dto.RoleDTO;
import com.label.admin.entity.SysPermission;
import com.label.admin.entity.SysRole;
import com.label.admin.service.RoleService;
import com.label.admin.util.RoleExcelData;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.label.admin.annotation.OperationLog;
import com.label.admin.annotation.RequiresPermission;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SysRole>> getRoleList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName) {
        IPage<SysRole> page = roleService.getRolePage(current, size, roleName);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysRole> getRoleById(@PathVariable Long id) {
        return Result.success(roleService.getRoleById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("角色新增")
    @OperationLog("新增角色")
    public Result<Void> createRole(@Valid @RequestBody RoleDTO dto) {
        roleService.createRole(dto);
        return Result.success("创建角色成功", null);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("角色编辑")
    @OperationLog("编辑角色")
    public Result<Void> updateRole(@Valid @RequestBody RoleDTO dto) {
        roleService.updateRole(dto);
        return Result.success("更新角色成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RequiresPermission("角色删除")
    @OperationLog("删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除角色成功", null);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("导出角色列表")
    public void exportRoles(
            @RequestParam(required = false) String roleName,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("角色列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        IPage<SysRole> page = roleService.getRolePage(1, 10000, roleName);
        List<RoleExcelData> dataList = new ArrayList<>();
        for (SysRole role : page.getRecords()) {
            RoleExcelData data = new RoleExcelData();
            data.setRoleName(role.getRoleName());
            data.setRoleCode(role.getRoleCode());
            data.setDescription(role.getDescription());
            data.setStatus(role.getStatus() == 1 ? "启用" : "禁用");
            data.setCreatedAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : "");
            dataList.add(data);
        }

        EasyExcel.write(response.getOutputStream(), RoleExcelData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("角色列表")
                .doWrite(dataList);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysPermission>> getAllPermissions() {
        return Result.success(roleService.getAllPermissions());
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissionIdsByRoleId(id));
    }
}
