package com.label.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.label.admin.dto.RoleDTO;
import com.label.admin.entity.SysPermission;
import com.label.admin.entity.SysRole;
import com.label.admin.entity.SysRolePermission;
import com.label.admin.entity.SysUserRole;
import com.label.admin.exception.BusinessException;
import com.label.admin.mapper.SysPermissionMapper;
import com.label.admin.mapper.SysRoleMapper;
import com.label.admin.mapper.SysRolePermissionMapper;
import com.label.admin.mapper.SysUserRoleMapper;
import com.label.admin.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper, SysRolePermissionMapper rolePermissionMapper,
                           SysPermissionMapper permissionMapper, SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public IPage<SysRole> getRolePage(int current, int size, String roleName) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        wrapper.orderByAsc(SysRole::getId);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysRole> getAllRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId)
        );
    }

    @Override
    public SysRole getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    @Override
    @Transactional
    public void createRole(RoleDTO dto) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode())
        );
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        roleMapper.insert(role);

        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            for (Long permId : dto.getPermissionIds()) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permId);
                rolePermissionMapper.insert(rp);
            }
        }

        log.info("创建角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional
    public void updateRole(RoleDTO dto) {
        SysRole existingRole = roleMapper.selectById(dto.getId());
        if (existingRole == null) {
            throw new BusinessException("角色不存在");
        }

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode())
                .ne(SysRole::getId, dto.getId());
        Long count = roleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        existingRole.setRoleName(dto.getRoleName());
        existingRole.setRoleCode(dto.getRoleCode());
        existingRole.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        if (dto.getStatus() != null) {
            existingRole.setStatus(dto.getStatus());
        }
        roleMapper.updateById(existingRole);

        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, dto.getId())
        );
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            for (Long permId : dto.getPermissionIds()) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(dto.getId());
                rp.setPermissionId(permId);
                rolePermissionMapper.insert(rp);
            }
        }

        log.info("更新角色成功: id={}", dto.getId());
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        if ("ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("系统管理员角色不能删除");
        }

        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id)
        );
        if (userCount > 0) {
            throw new BusinessException("该角色下有 " + userCount + " 个用户，无法删除");
        }

        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id)
        );
        roleMapper.deleteById(id);

        log.info("删除角色成功: id={}", id);
    }

    @Override
    public List<SysPermission> getAllPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getStatus, 1).orderByAsc(SysPermission::getOrderNum)
        );
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        return rolePermissions.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    public long countRoles() {
        return roleMapper.selectCount(null);
    }
}
