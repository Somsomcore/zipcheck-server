package somsomcore.zipcheck.domain.risk.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 👈
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import somsomcore.zipcheck.domain.risk.dto.RiskResponseDTO;
import somsomcore.zipcheck.domain.risk.service.RiskQueryService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risks")
public class RiskController {

    private final RiskQueryService riskService;

    @GetMapping("/my-list")
    @Operation(summary = "내 위험도 분석 목록 조회", description = "현재 로그인한 사용자가 진행했던 위험도 분석 목록을 페이징으로 조회합니다.")
    public ResponseEntity<ApiResponse<RiskResponseDTO.RiskListDTO>> getMyRiskList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size)
    {
        Sort sort = Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        RiskResponseDTO.RiskListDTO responseData = riskService.getRiskListByUser(
                userDetails.getUser().getId(), year, month, pageable);

        return ResponseEntity.ok(ApiResponse.onSuccess(responseData));
    }
}