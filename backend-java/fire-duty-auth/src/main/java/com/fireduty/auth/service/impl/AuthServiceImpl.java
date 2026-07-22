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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
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

        // 2. 校验密码（简化版，生产环境应使用BCryptPasswordEncoder）
        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 4. 获取用户权限
        List<String> authorities = getAuthorities(user.getRole());

        // 5. 生成JWT令牌
        String token = generateToken(user, authorities);
        String refreshToken = generateRefreshToken(user);

        // 6. 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        // 7. 构建返回
        UserInfoDTO userInfo = UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
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
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            // 2. 重新查询用户
            User user = userMapper.selectById(userId);
            if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            // 3. 获取权限并生成新令牌
            List<String> authorities = getAuthorities(role);
            String newToken = generateToken(user, authorities);
            String newRefreshToken = generateRefreshToken(user);

            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .role(user.getRole())
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

        List<String> authorities = getAuthorities(user.getRole());

        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .authorities(authorities)
                .gridId(user.getGridId())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * 生成JWT访问令牌
     */
    private String generateToken(User user, List<String> authorities) {
        SecretKey key = getSigningKey();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .claim("username", user.getUsername())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .claim("authorities", authorities)
                .signWith(key)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(User user) {
        SecretKey key = getSigningKey();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getRefreshExpiration());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration)
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .signWith(key)
                .compact();
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据角色获取权限列表
     */
    private List<String> getAuthorities(String role) {
        if (role == null) {
            return Collections.emptyList();
        }

        try {
            return permissionMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fireduty.auth.entity.Permission>()
                            .eq(com.fireduty.auth.entity.Permission::getRole, role)
            ).stream()
                    .map(p -> p.getResource() + ":" + p.getAction())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取权限失败，返回空权限列表", e);
            return Collections.emptyList();
        }
    }
}
