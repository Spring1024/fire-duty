package com.fireduty.common.aspect;

import com.fireduty.common.annotation.RequirePermission;
import com.fireduty.common.exception.ForbiddenException;
import com.fireduty.common.exception.UnauthorizedException;
import com.fireduty.common.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * AOP 切面：拦截所有带 @RequirePermission 注解的方法，
 * 从 JWT 中提取用户角色并进行权限校验。
 *
 * 取角色优先级：
 * 1. 请求头 X-User-Role（由 Gateway JwtAuthGatewayFilter 注入）
 * 2. 请求头 X-Role（兼容旧版）
 * 3. 请求属性 role（由其他过滤器设置）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(com.fireduty.common.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            return;
        }

        String resource = annotation.resource();
        String action = annotation.action();

        // 2. 从请求上下文中提取角色
        String role = extractRoleFromRequest();
        if (role == null || role.isBlank()) {
            log.warn("未授权访问: 缺少角色信息, resource={}, action={}", resource, action);
            throw new UnauthorizedException("未授权访问");
        }

        // 3. 校验权限
        boolean hasPermission = permissionService.hasPermission(role, resource, action);
        if (!hasPermission) {
            log.warn("权限不足: role={}, resource={}, action={}", role, resource, action);
            throw new ForbiddenException("权限不足，无法执行此操作");
        }

        log.debug("权限校验通过: role={}, resource={}, action={}", role, resource, action);
    }

    /**
     * 从当前请求中提取用户角色。
     * 优先级：X-User-Role header > X-Role header > request attribute
     */
    private String extractRoleFromRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        // 1) Gateway 注入的 JWT 角色
        String role = request.getHeader("X-User-Role");
        if (role != null && !role.isBlank()) {
            return role;
        }

        // 2) 兼容旧版 header
        role = request.getHeader("X-Role");
        if (role != null && !role.isBlank()) {
            return role;
        }

        // 3) request attribute（其他过滤器设置）
        Object attrRole = request.getAttribute("role");
        if (attrRole instanceof String sRole && !sRole.isBlank()) {
            return sRole;
        }

        return null;
    }
}
