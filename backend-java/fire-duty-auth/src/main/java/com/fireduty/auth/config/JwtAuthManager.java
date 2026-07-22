package com.fireduty.auth.config;

import com.fireduty.auth.entity.User;
import com.fireduty.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * JWT认证管理器 — 提供基于用户名密码的认证能力
 */
@Component
@RequiredArgsConstructor
public class JwtAuthManager implements AuthenticationManager {

    private final UserMapper userMapper;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 简化密码校验，生产环境应使用BCryptPasswordEncoder
        if (!password.equals(user.getPasswordHash())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BadCredentialsException("用户已被禁用");
        }

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null ? user.getRole().toUpperCase() : "USER"))
        );

        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}
