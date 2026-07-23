package com.fireduty.auth.controller;

import com.fireduty.auth.dto.LoginRequest;
import com.fireduty.auth.dto.LoginResponse;
import com.fireduty.auth.dto.PasswordChangeRequest;
import com.fireduty.auth.dto.RefreshTokenRequest;
import com.fireduty.auth.dto.UserInfoDTO;
import com.fireduty.auth.service.AuthService;
import com.fireduty.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    public UserInfoDTO me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return authService.getCurrentUser(userId);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody PasswordChangeRequest request) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @GetMapping("/check")
    public Map<String, Object> check(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", jwt.getSubject());
        result.put("username", jwt.getClaimAsString("username"));
        result.put("role", jwt.getClaimAsString("role"));
        result.put("roles", jwt.getClaimAsStringList("roles"));
        result.put("authorities", jwt.getClaimAsStringList("authorities"));
        return result;
    }
}
