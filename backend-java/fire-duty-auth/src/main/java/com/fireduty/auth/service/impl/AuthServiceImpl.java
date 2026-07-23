package com.fireduty.auth.service.impl;

import com.fireduty.auth.config.JwtConfig;
import com.fireduty.auth.dto.LoginRequest;
import com.fireduty.auth.dto.LoginResponse;
import com.fireduty.auth.dto.RefreshTokenRequest;
import com.fireduty.auth.dto.UserInfoDTO;
import com.fireduty.auth.entity.User;
import com.fireduty.auth.mapper.PermissionMapper;
import com.fireduty.auth.mapper.UserMapper;
import com.fireduty.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;
    private final JwtConfig jwtConfig;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());

        // 1. 查询用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("用户登录失败，密码错误: {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 4. 查询用户角色（从 user_roles + roles 表）
        List<String> roles = userMapper.selectRoleNamesByUserId(user.getId());

        // 5. 获取用户权限（聚合所有角色的权限）
        List<String> authorities = getAuthorities(roles);

        // 6. 生成JWT令牌
        String token = generateToken(user, roles, authorities);
        String refreshToken = generateRefreshToken(user, roles);

        // 7. 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        // 8. 构建返回
        UserInfoDTO userInfo = UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .roles(roles)
                .authorities(authorities)
                .gridId(user.getGridId())
                .lastLogin(user.getLastLogin())
                .build();

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        log.info("刷新令牌");

        try {
            // 1. 解析刷新令牌
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(request.getRefreshToken())
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());

            // 2. 重新查询用户
            User user = userMapper.selectById(userId);
            if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            // 3. 重新查询角色和权限
            List<String> roles = userMapper.selectRoleNamesByUserId(userId);
            List<String> authorities = getAuthorities(roles);
            String newToken = generateToken(user, roles, authorities);
            String newRefreshToken = generateRefreshToken(user, roles);

            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .roles(roles)
                    .authorities(authorities)
                    .gridId(user.getGridId())
                    .lastLogin(user.getLastLogin())
                    .build();

            return LoginResponse.builder()
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .userInfo(userInfo)
                    .build();

        } catch (ExpiredJwtException e) {
            log.error("刷新令牌已过期", e);
            throw new RuntimeException("刷新令牌已过期，请重新登录");
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            throw new RuntimeException("刷新令牌无效");
        }
    }

    @Override
    public UserInfoDTO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        List<String> roles = userMapper.selectRoleNamesByUserId(userId);
        List<String> authorities = getAuthorities(roles);

        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .roles(roles)
                .authorities(authorities)
                .gridId(user.getGridId())
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码 userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.warn("修改密码失败，旧密码错误 userId={}", userId);
            throw new RuntimeException("旧密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("密码修改成功 userId={}", userId);
    }

    /**
     * 生成JWT访问令牌
     */
    private String generateToken(User user, List<String> roles, List<String> authorities) {
        SecretKey key = getSigningKey();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());

        // 主角色取第一个（兼容旧逻辑）
        String primaryRole = (roles != null && !roles.isEmpty()) ? roles.get(0) : "";

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .claim("username", user.getUsername())
                .claim("name", user.getName())
                .claim("role", primaryRole)
                .claim("roles", roles)
                .claim("authorities", authorities)
                .signWith(key)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(User user, List<String> roles) {
        SecretKey key = getSigningKey();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getRefreshExpiration());

        String primaryRole = (roles != null && !roles.isEmpty()) ? roles.get(0) : "";

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .claim("username", user.getUsername())
                .claim("role", primaryRole)
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据角色列表获取权限列表（聚合所有角色的权限）
     */
    private List<String> getAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return permissionMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fireduty.auth.entity.Permission>()
                            .in(com.fireduty.auth.entity.Permission::getRole, roles)
            ).stream()
                    .map(p -> p.getResource() + ":" + p.getAction())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取权限失败，返回空权限列表", e);
            return Collections.emptyList();
        }
    }
}
