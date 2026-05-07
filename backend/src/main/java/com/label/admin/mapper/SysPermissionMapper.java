package com.label.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.label.admin.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    List<SysPermission> selectMenusByUserId(@Param("userId") Long userId);
}
