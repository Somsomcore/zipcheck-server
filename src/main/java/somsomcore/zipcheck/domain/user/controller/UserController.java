package somsomcore.zipcheck.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import somsomcore.zipcheck.domain.user.dto.UserProfileResponseDto;
import somsomcore.zipcheck.domain.user.service.UserService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @GetMapping
    public ApiResponse<UserProfileResponseDto> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponseDto response = userService.getUserProfile(userDetails.getUser());
        return ApiResponse.onSuccess(response);
    }
}