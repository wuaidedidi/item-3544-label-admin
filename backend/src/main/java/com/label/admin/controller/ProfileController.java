package com.label.admin.controller;

import com.label.admin.common.Result;
import com.label.admin.dto.ChangePasswordDTO;
import com.label.admin.dto.UpdateProfileDTO;
import com.label.admin.entity.SysPermission;
import com.label.admin.entity.SysUser;
import com.label.admin.mapper.SysPermissionMapper;
import com.label.admin.security.JwtUserDetails;
import com.label.admin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.label.admin.annotation.OperationLog;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final SysPermissionMapper permissionMapper;

    public ProfileController(UserService userService, SysPermissionMapper permissionMapper) {
        this.userService = userService;
        this.permissionMapper = permissionMapper;
    }

    @GetMapping
    public Result<SysUser> getCurrentUser(@AuthenticationPrincipal JwtUserDetails userDetails) {
        SysUser user = userService.getCurrentUserInfo(userDetails.getUserId());
        return Result.success(user);
    }

    @PutMapping
    @OperationLog("修改个人信息")
    public Result<SysUser> updateProfile(@RequestBody UpdateProfileDTO dto,
                                         @AuthenticationPrincipal JwtUserDetails userDetails) {
        userService.updateProfile(userDetails.getUserId(), dto);
        SysUser updatedUser = userService.getCurrentUserInfo(userDetails.getUserId());
        return Result.success("更新成功", updatedUser);
    }

    @PutMapping("/password")
    @OperationLog("修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                       @AuthenticationPrincipal JwtUserDetails userDetails) {
        userService.changePassword(userDetails.getUserId(), dto);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/menus")
    public Result<List<SysPermission>> getMenus(@AuthenticationPrincipal JwtUserDetails userDetails) {
        List<SysPermission> menus = permissionMapper.selectMenusByUserId(userDetails.getUserId());
        return Result.success(menus);
    }
}
