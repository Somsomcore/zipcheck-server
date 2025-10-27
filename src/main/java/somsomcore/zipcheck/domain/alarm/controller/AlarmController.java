package somsomcore.zipcheck.domain.alarm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import somsomcore.zipcheck.domain.alarm.dto.AlarmResponseDTO;
import somsomcore.zipcheck.domain.alarm.service.AlarmService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alarm")
@SecurityRequirement(name = "JWT TOKEN")
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "알림 목록 조회", description = "사용자에게 전달된 알림을 최신순으로 페이징 조회합니다.")
    @GetMapping
    public ApiResponse<AlarmResponseDTO.AlarmListResultDTO> getAlarms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AlarmResponseDTO.AlarmListResultDTO result = alarmService.getAlarms(userDetails.getUser().getId(), page, size);
        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "알림 확인 처리", description = "요청 시점까지 수신한 알림을 모두 확인 상태로 변경합니다.")
    @PostMapping("/confirm")
    public ApiResponse<AlarmResponseDTO.ConfirmResultDTO> confirmAlarms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AlarmResponseDTO.ConfirmResultDTO result = alarmService.confirmAllUntilNow(userDetails.getUser().getId());
        return ApiResponse.onSuccess(result);
    }
}
