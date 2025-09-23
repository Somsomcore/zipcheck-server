package somsomcore.zipcheck.domain.report.service.report;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.repository.report.ReportRepository;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;

    public ReportResponseDTO.MyReportsResultDTO getMyReports(Long userId, Pageable pageable) {
        Page<Report> reportPage = reportRepository.findByUserIdWithDetails(userId, pageable);

        List<ReportResponseDTO.ReportSummaryDTO> reportSummaries = reportPage.getContent()
                .stream()
                .map(this::convertToSummaryDTO)
                .toList();

        return ReportResponseDTO.MyReportsResultDTO.builder()
                .reports(reportSummaries)
                .totalPages(reportPage.getTotalPages())
                .currentPage(reportPage.getNumber())
                .totalElements(reportPage.getTotalElements())
                .isLast(reportPage.isLast())
                .build();
    }

    private ReportResponseDTO.ReportSummaryDTO convertToSummaryDTO(Report report) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return ReportResponseDTO.ReportSummaryDTO.builder()
                .id(report.getId())
                .address(report.getAddress().getAddr() + " " + report.getAddress().getAddrDetail())
                .content(report.getContent())
                .contractType(report.getContractType().getName())
                .contractedAt(dateFormat.format(report.getContractedAt()))
                .createdAt(report.getCreatedAt().format(dateTimeFormat))
                .build();
    }
}