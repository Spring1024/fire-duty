package com.fireduty.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解：标注在 Controller 方法上，标识需要特定权限。
 * 通过 AOP 切面拦截，从 JWT 中提取 role 并进行校验。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /** 资源名称，如 "devices", "tasks", "rectifications", "users", "statistics" */
    String resource();

    /** 操作类型，如 "read", "write", "delete" */
    String action();
}
