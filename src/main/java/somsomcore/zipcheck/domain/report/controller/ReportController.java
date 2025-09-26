package somsomcore.zipcheck.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.domain.report.converter.ReportConverter;
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;
import somsomcore.zipcheck.domain.report.service.report.ReportCommandService;
import somsomcore.zipcheck.domain.report.service.report.ReportQueryService;
import somsomcore.zipcheck.global.apiPayload.ApiResponse;
import somsomcore.zipcheck.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {
    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;

    // 사용자 사기 접수
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "사용자 신고 접수 API",
            description = "사용자가 신고글을 접수합니다.",
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

    // 사용자 신고글 삭제
    @Operation(summary = "신고글 삭제", description = "사용자가 특정 신고글을 삭제합니다.")
    @DeleteMapping
    public ApiResponse<String> deleteReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestParam("reportId") @Valid Long reportId) {
        reportCommandService.deleteReport(userDetails.getUser().getId(), reportId);
        return ApiResponse.onSuccess("신고글 삭제가 완료되었습니다.");
    }

    // 위도/경도 기반 신고글 주소 목록 조회
    @Operation(summary = "주변 신고 위치 목록 조회 API (지도에 핀 표시용)", description = "위도/경도를 기반으로 반경 안에 있는 신고글의 위치 목록을 조회합니다.(지도에 핀 표시용)")
    @GetMapping("/addrList")
    public ApiResponse<ReportResponseDTO.ReportAddrListResultDTO> getReportAddrList(
            @RequestParam(value = "lat") double lat,
            @RequestParam(value = "lng") double lng,
            @RequestParam(value = "radiusMeters") int radiusMeters) {

        ReportResponseDTO.ReportAddrListResultDTO response = reportQueryService.getReportAddrList(lat, lng, radiusMeters);
        return ApiResponse.onSuccess(response);
    }

    // 특정 주소의 신고글 목록 조회(탐색)
    @Operation(summary = "특정 주소의 신고글 목록 조회 API (탐색)", description = "특정 위치에 등록되어 있는 신고글 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<ReportResponseDTO.ReportListResultDTO> getReportList(
            @RequestParam(value = "addr") String addr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ReportResponseDTO.ReportListResultDTO response = reportQueryService.getReportList(addr, pageable);
        return ApiResponse.onSuccess(response);
    }

    // 내 신고글 목록 조회
    @Operation(summary = "내 신고글 목록 조회", description = "현재 로그인한 사용자가 작성한 신고글 목록을 페이징으로 조회합니다.")
    @SecurityRequirement(name = "JWT TOKEN")
    @GetMapping("/my")
    public ApiResponse<ReportResponseDTO.MyReportsResultDTO> getMyReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ReportResponseDTO.MyReportsResultDTO response = reportQueryService.getMyReports(userDetails.getUser().getId(), pageable);
        return ApiResponse.onSuccess(response);
    }

    // 사기 등록 조회(관리자)
    @Operation(summary = "사기 등록 조회(관리자)", description = "관리자가 사기 접수 목록을 페이징으로 조회합니다.(수락전/수락후)")
    @GetMapping("/admin")
    public ApiResponse<ReportResponseDTO.ReportsByStatusResultDTO> getPendingReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "status")RegistrationStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ReportResponseDTO.ReportsByStatusResultDTO response = reportQueryService.getReportsByStatus(userDetails.getUser().getId(), status, pageable);
        return ApiResponse.onSuccess(response);
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
