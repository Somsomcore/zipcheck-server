package somsomcore.zipcheck.domain.report.service.report;

import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.entity.Report;

public interface ReportCommandService {
    // 사용자 신고 접수
    Report addReport(Long memberId, ReportRequestDTO.addRequestReportDTO requestDTO, MultipartFile file);

    // 사용자 신고글 삭제
    void deleteReport(Long memberId, Long reportId);
}
