package somsomcore.zipcheck.domain.report.service.report;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.address.repository.AddressRepository;
import somsomcore.zipcheck.domain.report.converter.ReportConverter;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.repository.ReportAddressCount;
import somsomcore.zipcheck.domain.report.repository.report.ReportRepository;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;
    private final AddressRepository addressRepository;

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

    // 위도/경도 기반 신고글 주소 목록 조회
    public ReportResponseDTO.ReportAddrListResultDTO getReportAddrList(double lat, double lng, int radiusMeters) {
        // DB에서 모든 계산이 완료된 결과 목록을 한번에 가져옴
        List<ReportAddressCount> results = addressRepository.findAddressesInRadiusWithReportCount(lat, lng, radiusMeters);

        List<ReportResponseDTO.ReportAddrDTO> locations = results.stream()
                .map(result -> ReportResponseDTO.ReportAddrDTO.builder()
                        .latitude(result.getLatitude())
                        .longitude(result.getLongitude())
                        .address(result.getAddress())
                        .reportCount(result.getReportCount())
                        .build())
                .collect(Collectors.toList());

        return new ReportResponseDTO.ReportAddrListResultDTO(locations);
    }

    // 특정 주소의 신고글 목록 조회(탐색)
    public ReportResponseDTO.ReportListResultDTO getReportList(String addr, Pageable pageable){
        Page<Report> reportPage = reportRepository.findAllByAddressAddrContaining(addr, pageable);

        return ReportConverter.toReportListResultDTO(reportPage);
    }
}