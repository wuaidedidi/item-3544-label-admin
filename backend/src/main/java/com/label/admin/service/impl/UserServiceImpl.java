package com.label.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.label.admin.dto.ChangePasswordDTO;
import com.label.admin.dto.UpdateProfileDTO;
import com.label.admin.dto.UserCreateDTO;
import com.label.admin.dto.UserUpdateDTO;
import com.label.admin.entity.SysUser;
import com.label.admin.entity.SysUserRole;
import com.label.admin.exception.BusinessException;
import com.label.admin.mapper.SysUserMapper;
import com.label.admin.mapper.SysUserRoleMapper;
import com.label.admin.service.UserService;
import com.label.admin.util.SM2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SM2Util sm2Util;

    public UserServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                           PasswordEncoder passwordEncoder, SM2Util sm2Util) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.sm2Util = sm2Util;
    }

    @Override
    public IPage<SysUser> getUserPage(int current, int size, String username, Integer status) {
        Page<SysUser> page = new Page<>(current, size);
        IPage<SysUser> result = userMapper.selectUserPage(page, username, status);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public SysUser getUserById(Long id) {
        SysUser user = userMapper.selectUserWithRole(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public void createUser(UserCreateDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
        user.setPhone(dto.getPhone() != null ? dto.getPhone() : "");
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(dto.getRoleId());
        userRoleMapper.insert(userRole);

        log.info("创建用户成功: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void updateUser(UserUpdateDTO dto, Long currentUserId) {
        SysUser existingUser = userMapper.selectById(dto.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        SysUser currentUser = userMapper.selectUserWithRole(currentUserId);
        boolean isSelf = dto.getId().equals(currentUserId);

        if (isSelf && "ADMIN".equals(currentUser.getRoleCode())) {
            if (dto.getStatus() != null && dto.getStatus() == 0) {
                throw new BusinessException("管理员不能禁用自己的账号");
            }
            SysUser targetUser = userMapper.selectUserWithRole(dto.getId());
            if (targetUser != null && !dto.getRoleId().equals(targetUser.getRoleId())) {
                throw new BusinessException("管理员不能修改自己的角色");
            }
        }

        existingUser.setNickname(dto.getNickname());
        existingUser.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
        existingUser.setPhone(dto.getPhone() != null ? dto.getPhone() : "");
        if (dto.getStatus() != null) {
            existingUser.setStatus(dto.getStatus());
        }
        userMapper.updateById(existingUser);

        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, dto.getId())
        );
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(dto.getId());
        userRole.setRoleId(dto.getRoleId());
        userRoleMapper.insert(userRole);

        log.info("更新用户成功: id={}", dto.getId());
    }

    @Override
    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("不能删除自己的账号");
        }

        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        SysUser targetUser = userMapper.selectUserWithRole(id);
        if (targetUser != null && "ADMIN".equals(targetUser.getRoleCode())) {
            long adminCount = userRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, 1L)
            );
            if (adminCount <= 1) {
                throw new BusinessException("系统必须保留至少一个管理员账号");
            }
        }

        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id)
        );
        userMapper.deleteById(id);

        log.info("删除用户成功: id={}", id);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        userMapper.updateById(user);

        log.info("更新个人信息成功: userId={}", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();
        if (dto.isEncrypted()) {
            oldPassword = sm2Util.decrypt(oldPassword);
            newPassword = sm2Util.decrypt(newPassword);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("修改密码成功: userId={}", userId);
    }

    @Override
    public SysUser getCurrentUserInfo(Long userId) {
        SysUser user = userMapper.selectUserWithRole(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public long countUsers() {
        return userMapper.selectCount(null);
    }

    @Override
    public long countActiveUsers() {
        return userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1)
        );
    }
}
