package com.label.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.label.admin.dto.LoginRequest;
import com.label.admin.dto.RegisterRequest;
import com.label.admin.entity.SysUser;
import com.label.admin.entity.SysUserRole;
import com.label.admin.exception.BusinessException;
import com.label.admin.mapper.SysUserMapper;
import com.label.admin.mapper.SysUserRoleMapper;
import com.label.admin.security.JwtTokenProvider;
import com.label.admin.service.AuthService;
import com.label.admin.util.SM2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SM2Util sm2Util;

    public AuthServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                           PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                           SM2Util sm2Util) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sm2Util = sm2Util;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        String password = request.getPassword();
        if (request.isEncrypted()) {
            password = sm2Util.decrypt(password);
        }

        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String roleCode = user.getRoleCode() != null ? user.getRoleCode() : "USER";
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roleCode);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("roleCode", roleCode);
        userInfo.put("roleName", user.getRoleName());
        result.put("userInfo", userInfo);

        log.info("用户登录成功: {}", user.getUsername());
        return result;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        String password = request.getPassword();
        if (request.isEncrypted()) {
            password = sm2Util.decrypt(password);
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail() != null ? request.getEmail() : "");
        user.setPhone(request.getPhone() != null ? request.getPhone() : "");
        user.setStatus(1);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L);
        userRoleMapper.insert(userRole);

        log.info("用户注册成功: {}", user.getUsername());
    }
}
