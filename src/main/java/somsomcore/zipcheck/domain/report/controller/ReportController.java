package somsomcore.zipcheck.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.domain.report.converter.ReportConverter;
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.service.report.ReportCommandService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {
    private final ReportCommandService reportCommandService;

    // 사용자 신고 접수
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사용자 신고 접수 API")
    public ApiResponse<ReportResponseDTO.addReportResultDTO> addReport(
            @RequestPart("request") @Valid ReportRequestDTO.addRequestReportDTO request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Report report = reportCommandService.addReport(1L, request, file);
        return ApiResponse.onSuccess(ReportConverter.toReportResultDTO(report));
    }
}
