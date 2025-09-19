package somsomcore.zipcheck.domain.report.dto.report;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class ReportRequestDTO {

    // 사용자 사기 접수
    @Getter
    @Setter
    public static class addRequestReportDTO {
        private String addr;  // 주소
        private String addrDetail;   // 상세주소
        private Long classification;  // 사기분류
        private Long contractType;    // 계약 형태
        private Date contractedAt;  // 계약 일자
        private Date recognitionAt; // 사기 인지 일자
        private String content;  // 피해 내용
    }

}
