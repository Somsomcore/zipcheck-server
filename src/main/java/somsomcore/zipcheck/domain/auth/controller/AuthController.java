package somsomcore.zipcheck.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import somsomcore.zipcheck.domain.auth.dto.AuthResponseDto;
import somsomcore.zipcheck.domain.auth.dto.SocialLoginRequestDto;
import somsomcore.zipcheck.domain.auth.dto.TokenRefreshRequestDto;
import somsomcore.zipcheck.domain.auth.dto.TokenRefreshResponseDto;
import somsomcore.zipcheck.domain.auth.service.AuthService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "카카오 또는 네이버를 통한 소셜 로그인을 처리합니다.")
    @PostMapping
    public ApiResponse<AuthResponseDto> socialLogin(@Valid @RequestBody SocialLoginRequestDto request) {
        AuthResponseDto response = authService.socialLogin(request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponseDto> refreshToken(@Valid @RequestBody TokenRefreshRequestDto request) {
        TokenRefreshResponseDto response = authService.refreshToken(request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUser().getId());
        return ApiResponse.onSuccess("로그아웃이 완료되었습니다.");
    }
}