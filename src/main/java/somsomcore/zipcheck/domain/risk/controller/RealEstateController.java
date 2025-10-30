package somsomcore.zipcheck.domain.risk.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import somsomcore.zipcheck.domain.risk.dto.RentFilterRequestDto;
import somsomcore.zipcheck.domain.risk.dto.RiskResponseDTO;
import somsomcore.zipcheck.domain.risk.service.RealEstateService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;


@RestController
@RequestMapping("/api/real-estate")
@RequiredArgsConstructor
public class RealEstateController {

    private final RealEstateService realEstateService;

    @PostMapping("/rent/analyze/{regionCode}")
    @Operation(summary = "위험도 분석", description = "위험도 분석을 진행합니다")
    public ResponseEntity<ApiResponse<RiskResponseDTO.RiskDetailDTO>> analyzeAndSaveRisk(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String regionCode,
            @Valid @RequestBody RentFilterRequestDto filterDto) {

        RiskResponseDTO.RiskDetailDTO savedRisk = realEstateService.analyzeAndSaveRisk(regionCode, filterDto, userDetails.getUser());

        return ResponseEntity.ok(ApiResponse.onSuccess(savedRisk));
    }
}