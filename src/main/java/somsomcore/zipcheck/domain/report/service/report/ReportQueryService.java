package somsomcore.zipcheck.domain.report.service.report;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.address.repository.AddressRepository;
import somsomcore.zipcheck.domain.report.converter.ReportConverter;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;
import somsomcore.zipcheck.domain.report.repository.ReportAddressCount;
import somsomcore.zipcheck.domain.report.repository.ReportWithAddressCount;
import somsomcore.zipcheck.domain.report.repository.report.ReportRepository;
import somsomcore.zipcheck.domain.risk.entity.Risk;
import somsomcore.zipcheck.domain.risk.repository.RiskRepository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.domain.user.repository.UserRepository;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.handler.ReportHandler;
import somsomcore.zipcheck.global.apiPayload.exception.handler.UserHandler;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final RiskRepository riskRepository;
    private static final int NEARBY_RADIUS_METERS = 10000; // 10km

    public ReportResponseDTO.MyReportsResultDTO getMyReports(Long userId, RegistrationStatus status, Pageable pageable) {
        Page<Report> reportPage = reportRepository.findByUserIdAndStatusWithDetails(userId, status, pageable);

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
                .addr(report.getAddress().getAddr())
                .addrDetail(report.getAddress().getAddrDetail())
                .content(report.getContent())
                .contractType(report.getContractType().getName())
                .classificationId(report.getClassification().getId())
                .classificationName(report.getClassification().getName())
                .contractedAt(dateFormat.format(report.getContractedAt()))
                .createdAt(report.getCreatedAt().format(dateTimeFormat))
                .build();
    }

    // 위도/경도 기반 신고글 주소 목록 조회
    public ReportResponseDTO.ReportAddrListResultDTO getReportAddrList(double lat, double lng, int radiusMeters) {
        // DB에서 모든 계산이 완료된 결과 목록을 한번에 가져옴
        List<ReportAddressCount> results = addressRepository.findGroupedAddressesInRadius(lat, lng, radiusMeters);

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
        Page<Report> reportPage = reportRepository.findAllByAddressAddrContainingAndRegistrationStatus(addr, RegistrationStatus.APPROVED, pageable);

        return ReportConverter.toReportListResultDTO(reportPage);
    }

    // 사기 등록 조회(관리자)
    public ReportResponseDTO.ReportsByStatusResultDTO getReportsByStatus(Long userId, RegistrationStatus status, Pageable pageable){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 권한 없음 예외 처리
        if (user.getRole() != Role.ADMIN) {
            throw new UserHandler(ErrorStatus.USER_FORBIDDEN);
        }

        Page<Report> reportPage = reportRepository.findAllByRegistrationStatus(status, pageable);

        return ReportConverter.toRegistrationStatusReportsResultDTO(reportPage);
    }

    // 신고 글 상세보기(관리자)
    public ReportResponseDTO.ReportDetailDTO getReport(Long userID, Long reportId){
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 권한 없음 예외 처리
        if (user.getRole() != Role.ADMIN) {
            throw new UserHandler(ErrorStatus.USER_FORBIDDEN);
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_NOT_FOUND));

        return ReportConverter.toReportDetailDTO(report);
    }

    // 메인 TOP 5 API
    public ReportResponseDTO.Top5ReportsResultDTO getTop5Reports(Long userId) {
        List<ReportWithAddressCount> top5List;

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

            Optional<Risk> latestRiskOpt = riskRepository.findTopByUserOrderByCreatedAtDesc(user);

            if (latestRiskOpt.isPresent()) {
                Address riskAddress = latestRiskOpt.get().getAddress();
                top5List = reportRepository.findTop5ReportsByLocation(
                        riskAddress.getLat(),
                        riskAddress.getLng(),
                        NEARBY_RADIUS_METERS
                );
            } else {
                top5List = reportRepository.findTop5ReportsNationwide();
            }
        } else {
            top5List = reportRepository.findTop5ReportsNationwide();
        }

        return ReportConverter.toTop5ReportsResultDTO(top5List);
    }
}
