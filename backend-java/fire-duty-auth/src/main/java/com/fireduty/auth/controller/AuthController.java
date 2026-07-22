package com.fireduty.auth.controller;

import com.fireduty.auth.dto.LoginRequest;
import com.fireduty.auth.dto.LoginResponse;
import com.fireduty.auth.dto.RefreshTokenRequest;
import com.fireduty.auth.dto.UserInfoDTO;
import com.fireduty.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDTO> me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        UserInfoDTO userInfo = authService.getCurrentUser(userId);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> check(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
                "userId", jwt.getSubject(),
                "username", jwt.getClaimAsString("username"),
                "role", jwt.getClaimAsString("role"),
                "authorities", jwt.getClaimAsStringList("authorities")
        ));
    }
}
