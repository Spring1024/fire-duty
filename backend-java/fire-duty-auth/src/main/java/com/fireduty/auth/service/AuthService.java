package com.fireduty.auth.service;

import com.fireduty.auth.dto.LoginRequest;
import com.fireduty.auth.dto.LoginResponse;
import com.fireduty.auth.dto.RefreshTokenRequest;
import com.fireduty.auth.dto.UserInfoDTO;

public interface AuthService {

    /**
     * 用户登录：校验用户名密码并生成JWT令牌
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新JWT令牌
     */
    LoginResponse refresh(RefreshTokenRequest request);

    /**
     * 获取当前登录用户信息
     */
    UserInfoDTO getCurrentUser(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
