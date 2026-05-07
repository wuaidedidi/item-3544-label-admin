package com.label.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.label.admin.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectUserWithRole(@Param("userId") Long userId);

    SysUser selectByUsername(@Param("username") String username);

    IPage<SysUser> selectUserPage(Page<SysUser> page, @Param("username") String username, @Param("status") Integer status);
}
