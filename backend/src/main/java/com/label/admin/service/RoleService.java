package com.label.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.label.admin.dto.RoleDTO;
import com.label.admin.entity.SysPermission;
import com.label.admin.entity.SysRole;

import java.util.List;

public interface RoleService {

    IPage<SysRole> getRolePage(int current, int size, String roleName);

    List<SysRole> getAllRoles();

    SysRole getRoleById(Long id);

    void createRole(RoleDTO dto);

    void updateRole(RoleDTO dto);

    void deleteRole(Long id);

    List<SysPermission> getAllPermissions();

    List<Long> getPermissionIdsByRoleId(Long roleId);

    long countRoles();
}
