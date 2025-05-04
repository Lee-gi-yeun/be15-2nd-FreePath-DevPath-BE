package com.freepath.devpath.common.auth.controller;

import com.freepath.devpath.common.auth.dto.*;
import com.freepath.devpath.common.auth.service.AuthService;
import com.freepath.devpath.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 관리", description = "로그인, 로그아웃, 토큰 갱신, 회원 탈퇴 등 인증 관련 기능 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "일반 로그인", description = "loginId와 password를 이용해 로그인하고 토큰을 발급받습니다")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 이용해 액세스 토큰을 재발급받습니다")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 만료시켜 로그아웃 처리합니다")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 탈퇴", description = "사용자가 자신의 계정을 탈퇴합니다. 인증 정보와 이메일 필요")
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal User user,
            @RequestBody @Validated UserDeleteRequest request
    ) {
        String userId = user.getUsername();
        authService.deleteUser(userId, request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
