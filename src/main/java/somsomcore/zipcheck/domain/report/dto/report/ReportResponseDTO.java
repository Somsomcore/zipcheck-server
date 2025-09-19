package somsomcore.zipcheck.domain.report.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

}
