package somsomcore.zipcheck.domain.report.converter;

import org.springframework.data.domain.Page;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.repository.ReportWithAddressCount;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportConverter {
    public static ReportResponseDTO.addReportResultDTO toReportResultDTO(Report report) {
        return ReportResponseDTO.addReportResultDTO.builder()
                .memberId(report.getUser().getId())
                .reportId(report.getId())
                .registrationStatus(String.valueOf(report.getRegistrationStatus()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 특정 주소의 신고글 목록 조회(탐색)
    public static ReportResponseDTO.ReportDTO toReportDTO(Report report) {
        return ReportResponseDTO.ReportDTO.builder()
                .reportId(report.getId())
                .addr(report.getAddress().getAddr())
                .addrDetail(report.getAddress().getAddrDetail())
                .classification(report.getClassification().getId()) // ID 값만 추출
                .contractType(report.getContractType().getId())   // ID 값만 추출
                .content(report.getContent())
                .contractAt(report.getContractedAt())
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 특정 주소의 신고글 목록 조회(탐색)
    // Page<Report>를 ReportListResultDTO로 변환
    public static ReportResponseDTO.ReportListResultDTO toReportListResultDTO(Page<Report> reportPage) {
        List<ReportResponseDTO.ReportDTO> reportDTOList = reportPage.getContent().stream()
                .map(ReportConverter::toReportDTO)
                .collect(Collectors.toList());

        return ReportResponseDTO.ReportListResultDTO.builder()
                .reports(reportDTOList)
                .totalPages(reportPage.getTotalPages())
                .currentPage(reportPage.getNumber())
                .totalElements(reportPage.getTotalElements())
                .isLast(reportPage.isLast())
                .build();
    }

    // 사기 등록 조회(관리자)
    public static ReportResponseDTO.ReportByStatus toRegistrationStatusReportDTO(Report report) {

        return ReportResponseDTO.ReportByStatus.builder()
                .reportId(report.getId())
                .name(report.getUser().getName())
                .addr(report.getAddress().getAddr())
                .addrDetail(report.getAddress().getAddrDetail())
                .contractType(report.getContractType().getId())
                .content(report.getContent())
                .contractAt(report.getContractedAt())
                .isRegistration(report.getRegistrationStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 사기 등록 조회(관리자)
    public static ReportResponseDTO.ReportsByStatusResultDTO toRegistrationStatusReportsResultDTO(Page<Report> reportPage) {
        List<ReportResponseDTO.ReportByStatus> reportDTOList = reportPage.getContent().stream()
                .map(ReportConverter::toRegistrationStatusReportDTO)
                .collect(Collectors.toList());

        return ReportResponseDTO.ReportsByStatusResultDTO.builder()
                .reports(reportDTOList)
                .totalPages(reportPage.getTotalPages())
                .currentPage(reportPage.getNumber())
                .totalElements(reportPage.getTotalElements())
                .isLast(reportPage.isLast())
                .build();
    }

    // 사기 등록 상태 변경(관리자-거절/수락)
    public static ReportResponseDTO.ChageStatusOfReportDTO toChangeStatusOfReportDTO(Report report) {
        return ReportResponseDTO.ChageStatusOfReportDTO.builder()
                .reportId(report.getId())
                .registrationStatus(report.getRegistrationStatus())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    // 신고 글 상세보기(관리자)
    public static ReportResponseDTO.ReportDetailDTO toReportDetailDTO(Report report) {
        return ReportResponseDTO.ReportDetailDTO.builder()
                .reportId(report.getId())
                .name(report.getUser().getName())
                .addr(report.getAddress().getAddr())
                .addrDetail(report.getAddress().getAddrDetail())
                .classification(report.getClassification().getId())
                .contractType(report.getContractType().getId())
                .content(report.getContent())
                .document_key(report.getDocumentUrl())
                .isRegistration(report.getRegistrationStatus())
                .recognitionAt(report.getRecognitionAt())
                .contractAt(report.getContractedAt())
                .createdAt(report.getCreatedAt())
                .build();
    }

    // 메인 TOP 5 API
    public static ReportResponseDTO.TopReportLocationDTO toTopReportLocationDTO(ReportWithAddressCount projection) {
        return ReportResponseDTO.TopReportLocationDTO.builder()
                .reportId(projection.getReportId())
                .addr(projection.getAddr())
                .addrDetail(projection.getAddrDetail())
                .classification(projection.getClassificationId())
                .contractType(projection.getContractTypeId())
                .count(projection.getCount())
                .build();
    }

    public static ReportResponseDTO.Top5ReportsResultDTO toTop5ReportsResultDTO(List<ReportWithAddressCount> projectionList) {
        List<ReportResponseDTO.TopReportLocationDTO> dtoList = projectionList.stream()
                .map(ReportConverter::toTopReportLocationDTO)
                .collect(Collectors.toList());

        return ReportResponseDTO.Top5ReportsResultDTO.builder()
                .reports(dtoList)
                .build();
    }
}
