package somsomcore.zipcheck.domain.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.domain.report.converter.ReportConverter;
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.service.report.ReportCommandService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {
    private final ReportCommandService reportCommandService;

    // 사용자 사기 접수
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "사용자 신고 접수 API",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ReportUploadSchema.class),
                            encoding = {
                                    @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "file",    contentType = MediaType.APPLICATION_PDF_VALUE)
                            }
                    )
            )
    )
    public ApiResponse<ReportResponseDTO.addReportResultDTO> addReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid ReportRequestDTO.addRequestReportDTO request,
            @RequestPart("file") MultipartFile file
    ) {
        Report report = reportCommandService.addReport(userDetails.getUser().getId(), request, file);
        return ApiResponse.onSuccess(ReportConverter.toReportResultDTO(report));
    }

    /** Swagger에서 멀티파트 각 파트를 정의하기 위한 스키마 */
    @Schema(name = "ReportUploadSchema")
    static class ReportUploadSchema {
        @Schema(description = "신고 JSON", implementation = ReportRequestDTO.addRequestReportDTO.class)
        public ReportRequestDTO.addRequestReportDTO request;

        @Schema(type = "string", format = "binary", description = "첨부 파일 (PDF)")
        public MultipartFile file;
    }
}
