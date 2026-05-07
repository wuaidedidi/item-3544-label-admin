package com.label.admin.controller;

import com.label.admin.common.Result;
import com.label.admin.service.FileService;
import com.label.admin.service.RoleService;
import com.label.admin.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserService userService;
    private final RoleService roleService;
    private final FileService fileService;

    public DashboardController(UserService userService, RoleService roleService, FileService fileService) {
        this.userService = userService;
        this.roleService = roleService;
        this.fileService = fileService;
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countUsers());
        stats.put("activeUsers", userService.countActiveUsers());
        stats.put("totalRoles", roleService.countRoles());
        stats.put("totalFiles", fileService.countFiles());
        return Result.success(stats);
    }

    @GetMapping("/admin-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countUsers());
        stats.put("activeUsers", userService.countActiveUsers());
        stats.put("totalRoles", roleService.countRoles());
        stats.put("totalFiles", fileService.countFiles());
        return Result.success(stats);
    }
}
