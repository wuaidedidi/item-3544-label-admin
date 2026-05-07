package com.label.admin.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 - 标记在Controller方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述
     */
    String value() default "";
}
