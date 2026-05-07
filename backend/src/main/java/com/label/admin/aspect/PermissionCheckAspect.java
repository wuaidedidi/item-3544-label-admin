package com.label.admin.aspect;

import com.label.admin.annotation.RequiresPermission;
import com.label.admin.entity.SysPermission;
import com.label.admin.exception.BusinessException;
import com.label.admin.mapper.SysPermissionMapper;
import com.label.admin.security.JwtUserDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 细粒度权限检查切面 - 根据 @RequiresPermission 注解检查用户是否拥有对应按钮权限
 */
@Aspect
@Component
public class PermissionCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionCheckAspect.class);

    private final SysPermissionMapper permissionMapper;

    public PermissionCheckAspect(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserDetails userDetails)) {
            throw new BusinessException(401, "未登录或登录已过期");
        }

        // ADMIN角色拥有所有权限，直接放行
        if ("ADMIN".equals(userDetails.getRoleCode())) {
            return joinPoint.proceed();
        }

        String requiredPermName = requiresPermission.value();
        List<SysPermission> userPermissions = permissionMapper.selectPermissionsByUserId(userDetails.getUserId());

        boolean hasPermission = userPermissions.stream()
                .anyMatch(p -> requiredPermName.equals(p.getName()));

        if (!hasPermission) {
            log.warn("用户 {} 缺少权限: {}", userDetails.getUsername(), requiredPermName);
            throw new BusinessException(403, "没有操作权限: " + requiredPermName);
        }

        return joinPoint.proceed();
    }
}
