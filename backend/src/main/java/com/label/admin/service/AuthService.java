package com.label.admin.service;

import com.label.admin.dto.LoginRequest;
import com.label.admin.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(LoginRequest request);

    void register(RegisterRequest request);
}
