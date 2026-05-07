package com.label.admin.controller;

import com.label.admin.common.Result;
import com.label.admin.dto.LoginRequest;
import com.label.admin.dto.RegisterRequest;
import com.label.admin.service.AuthService;
import com.label.admin.util.SM2Util;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.label.admin.annotation.OperationLog;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SM2Util sm2Util;

    public AuthController(AuthService authService, SM2Util sm2Util) {
        this.authService = authService;
        this.sm2Util = sm2Util;
    }

    @PostMapping("/login")
    @OperationLog("用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = authService.login(request);
        return Result.success("登录成功", data);
    }

    @PostMapping("/register")
    @OperationLog("用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功", null);
    }

    @GetMapping("/sm2/public-key")
    public Result<String> getPublicKey() {
        return Result.success(sm2Util.getPublicKey());
    }
}
