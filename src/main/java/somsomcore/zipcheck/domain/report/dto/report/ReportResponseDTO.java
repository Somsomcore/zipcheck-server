package somsomcore.zipcheck.domain.report.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ReportResponseDTO {

    // 사용자 사기 접수
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class addReportResultDTO {
        private Long memberId;     // 사용자 ID
        private Long reportId;     // 접수된 신고글 ID
        private String registrationStatus; // 신고글 관리자 등록 허가 상태
        private LocalDateTime createdAt;  // 접수 일자
    }

    // 내 신고글 목록 조회
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyReportsResultDTO {
        private List<ReportSummaryDTO> reports;
        private Integer totalPages;
        private Integer currentPage;
        private Long totalElements;
        private Boolean isLast;
    }

    // 신고글 요약 정보
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportSummaryDTO {
        private Long id;
        private String address;
        private String content;
        private String contractType;
        private String contractedAt;
        private String createdAt;
    }

    // 주변 신고 위치 목록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportAddrListResultDTO {
        private List<ReportAddrDTO> locations;
    }

    // 주변 신고 위치
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportAddrDTO {
        private double latitude;
        private double longitude;
        private String address;
        private Long reportCount;
    }

    // 주변 신고 위치 목록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportListResultDTO {
        private List<ReportDTO> reports;
        private Integer totalPages;
        private Integer currentPage;
        private Long totalElements;
        private Boolean isLast;
    }

    // 주변 신고 위치
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportDTO {
        private Long reportId;
        private String addr;
        private String addrDetail;
        private Long classification;
        private Long contractType;
        private String content;
        private Date contractAt;
        private LocalDateTime createdAt;
    }

    // 사기 등록 조회(관리자) - 접수된 사기 목록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportsByStatusResultDTO {
        private List<ReportByStatus> reports;
        private Integer totalPages;
        private Integer currentPage;
        private Long totalElements;
        private Boolean isLast;
    }

    // 사기 등록 조회(관리자) - 접수된 사기
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportByStatus {
        private Long reportId;
        private String name;
        private String addr;
        private String addrDetail;
        private Long contractType;
        private String content;
        private Date contractAt;
        private RegistrationStatus isRegistration;
        private LocalDateTime createdAt;
    }

    // 사기 등록 상태 변경(관리자-거절/수락)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChageStatusOfReportDTO {
        private Long reportId;     // 변경된 신고글 ID
        private RegistrationStatus registrationStatus; // 신고글 관리자 등록 허가 상태
        private LocalDateTime updatedAt;  // 수정 일자
    }

    // 신고 글 상세보기(관리자)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportDetailDTO {
        private Long reportId;
        private String name;
        private String addr;
        private String addrDetail;
        private Long classification;
        private Long contractType;
        private String content;
        private String document_key;
        private RegistrationStatus isRegistration;
        private Date recognitionAt;
        private Date contractAt;
        private LocalDateTime createdAt;
    }

    // 메인 TOP 5 API 전체 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Top5ReportsResultDTO {
        private List<TopReportLocationDTO> reports;
    }

    // 메인 TOP 5 API 개별 응답 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopReportLocationDTO {
        private String addr;
        private String addrDetail;
        private List<ClassificationIdDto> classifications;
        private long contractType;
        private long count; // 해당 주소의 총 신고 횟수
    }

    // 메인 TOP 5 API 사기 분류 DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassificationIdDto {
        private long classification;
    }
}
