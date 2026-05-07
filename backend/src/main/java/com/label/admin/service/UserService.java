package com.label.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.label.admin.dto.ChangePasswordDTO;
import com.label.admin.dto.UpdateProfileDTO;
import com.label.admin.dto.UserCreateDTO;
import com.label.admin.dto.UserUpdateDTO;
import com.label.admin.entity.SysUser;

public interface UserService {

    IPage<SysUser> getUserPage(int current, int size, String username, Integer status);

    SysUser getUserById(Long id);

    SysUser getUserByUsername(String username);

    void createUser(UserCreateDTO dto);

    void updateUser(UserUpdateDTO dto, Long currentUserId);

    void deleteUser(Long id, Long currentUserId);

    void updateProfile(Long userId, UpdateProfileDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);

    SysUser getCurrentUserInfo(Long userId);

    long countUsers();

    long countActiveUsers();
}
