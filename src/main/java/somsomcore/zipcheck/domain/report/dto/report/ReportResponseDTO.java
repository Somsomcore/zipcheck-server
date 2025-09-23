package somsomcore.zipcheck.domain.report.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

}
