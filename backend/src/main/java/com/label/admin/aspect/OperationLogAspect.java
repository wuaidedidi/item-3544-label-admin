package com.label.admin.aspect;

import com.label.admin.annotation.OperationLog;
import com.label.admin.entity.SysOperationLog;
import com.label.admin.mapper.SysOperationLogMapper;
import com.label.admin.security.JwtUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final SysOperationLogMapper operationLogMapper;

    public OperationLogAspect(SysOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setOperation(operationLog.value());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        logEntity.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

        try {
            String params = Arrays.toString(joinPoint.getArgs());
            if (params.length() > 2000) {
                params = params.substring(0, 2000);
            }
            logEntity.setParams(params);
        } catch (Exception e) {
            logEntity.setParams("参数序列化失败");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails userDetails) {
            logEntity.setUserId(userDetails.getUserId());
            logEntity.setUsername(userDetails.getUsername());
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Real-IP");
            if (ip == null || ip.isEmpty()) {
                ip = request.getHeader("X-Forwarded-For");
            }
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            logEntity.setIp(ip);
        }

        Object result;
        try {
            result = joinPoint.proceed();
            logEntity.setStatus(1);
        } catch (Exception e) {
            logEntity.setStatus(0);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 2000);
            }
            logEntity.setErrorMsg(errorMsg);
            throw e;
        } finally {
            logEntity.setDuration(System.currentTimeMillis() - startTime);
            try {
                operationLogMapper.insert(logEntity);
            } catch (Exception e) {
                log.error("操作日志写入失败: {}", e.getMessage());
            }
        }
        return result;
    }
}
