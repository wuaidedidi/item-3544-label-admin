package com.label.admin.annotation;

import java.lang.annotation.*;

/**
 * 细粒度权限检查注解 - 检查用户是否拥有指定的权限（基于sys_permission表的name字段）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 需要的权限名称（对应sys_permission.name）
     */
    String value();
}
