package somsomcore.zipcheck.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import somsomcore.zipcheck.domain.auth.dto.*;
import somsomcore.zipcheck.domain.auth.service.AuthService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "카카오 또는 네이버 액세스 토큰과 전화번호(신규 가입 시 필수)를 이용해 로그인/가입을 처리합니다.")
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

    @Operation(summary = "테스트 토큰 생성", description = "API 테스트용 임시 토큰을 생성합니다.")
    @PostMapping("/test-token")
    public ApiResponse<AuthResponseDto> generateTestToken(@Valid @RequestBody TestTokenRequestDto request) {
        AuthResponseDto response = authService.generateTestToken(request);
        return ApiResponse.onSuccess(response);
    }

	@Operation(summary = "본인 인증 문자 메시지 발송", description = "본인 인증을 위한 문자 메시지를 발송합니다.")
	@PostMapping("/verification-code")
	public ApiResponse<String> sendValidationMessage(@Valid @RequestBody ValidationMessageRequestDto request,
													 @AuthenticationPrincipal CustomUserDetails userDetails) {
		authService.sendValidationMessage(userDetails.getUser().getId(), request);
		return ApiResponse.onSuccess("성공적으로 메시지를 전송했습니다.");
	}

	@Operation(summary = "본인 인증 코드 검증", description = "사용자가 입력한 본인 인증 코드를 검증합니다.")
	@PostMapping("/verification")
	public ApiResponse<String> validateUserPhone(@Valid @RequestBody ValidatePhoneRequestDto request,
												 @AuthenticationPrincipal CustomUserDetails userDetails) {
		authService.validateUserPhone(userDetails.getUser().getId(), request);
		return ApiResponse.onSuccess("성공적으로 인증되었습니다.");
	}
}
