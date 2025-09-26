package somsomcore.zipcheck.domain.report.service.report;

import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;

public interface ReportCommandService {
    // 사용자 신고 접수
    Report addReport(Long memberId, ReportRequestDTO.addRequestReportDTO requestDTO, MultipartFile file);

    // 사용자 신고글 삭제
    void deleteReport(Long memberId, Long reportId);

    // 사기 등록 상태 변경(관리자-거절/수락)
    ReportResponseDTO.ChageStatusOfReportDTO changeStatusOfReport(Long userId, Long reportId, ReportRequestDTO.ChangeStatusRequestDTO request);
}
