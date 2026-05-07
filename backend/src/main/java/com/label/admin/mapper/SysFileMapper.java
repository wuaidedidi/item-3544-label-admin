package com.label.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.label.admin.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {

    IPage<SysFile> selectFilePage(Page<SysFile> page, @Param("originalName") String originalName, @Param("uploaderId") Long uploaderId);
}
