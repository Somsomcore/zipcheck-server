package somsomcore.zipcheck.domain.report.converter;

import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;

import java.time.LocalDateTime;

public class ReportConverter {
    public static ReportResponseDTO.addReportResultDTO toReportResultDTO(Report report) {
        return ReportResponseDTO.addReportResultDTO.builder()
                .memberId(report.getUser().getId())
                .reportId(report.getId())
                .registrationStatus(String.valueOf(report.getRegistrationStatus()))
                .createdAt(LocalDateTime.now())
                .build();
    }

}
