package somsomcore.zipcheck.domain.report.service.report;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.address.repository.AddressRepository;
import somsomcore.zipcheck.domain.alarm.service.AlarmService;
import somsomcore.zipcheck.domain.report.converter.ReportConverter;
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.dto.report.ReportResponseDTO;
import somsomcore.zipcheck.domain.report.entity.Classification;
import somsomcore.zipcheck.domain.report.entity.ContractType;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;
import somsomcore.zipcheck.domain.report.repository.ClassificationRepository;
import somsomcore.zipcheck.domain.report.repository.ContractTypeRepository;
import somsomcore.zipcheck.domain.report.repository.report.ReportRepository;
import somsomcore.zipcheck.domain.s3.service.S3Service;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.domain.user.repository.UserRepository;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.handler.AddressHandler;
import somsomcore.zipcheck.global.apiPayload.exception.handler.ContractTypeHandler;
import somsomcore.zipcheck.global.apiPayload.exception.handler.ReportHandler;
import somsomcore.zipcheck.global.apiPayload.exception.handler.UserHandler;

@Service
@RequiredArgsConstructor
public class ReportCommandServiceImpl implements ReportCommandService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final ClassificationRepository classificationRepository;
    private final S3Service s3Service;
    private final GeoApiContext geoApiContext;
    private final AlarmService alarmService;

    // 사용자 사기 접수
    @Override
    @Transactional
    public Report addReport(Long memberId, ReportRequestDTO.addRequestReportDTO requestDTO, MultipartFile file){
        // 회원 검증
        User user = verifyUser(memberId);

        // 계약 형태
        ContractType contractType = contractTypeRepository.findById(requestDTO.getContractType()).orElseThrow(() -> new ContractTypeHandler(ErrorStatus.CONTRACTTYPE_NOT_FOUND));

        // 사기 분류
        Classification classification = classificationRepository.findById(requestDTO.getClassification()).orElseThrow(() -> new ContractTypeHandler(ErrorStatus.CLASSIFICATION_NOT_FOUND));

        // 주소 처리: DB에 없으면 지오코딩으로 lat/lng 조회 후 저장
        Address address = addressRepository
                .findByAddrAndAddrDetail(requestDTO.getAddr(), requestDTO.getAddrDetail())
                .map(existingAddress -> {
                    existingAddress.setCount(existingAddress.getCount() + 1);
                    return existingAddress;
                })
                .orElseGet(() -> {
                    try {
                        String base = requestDTO.getAddr() == null ? "" : requestDTO.getAddr().trim();
                        String detail = requestDTO.getAddrDetail() == null ? "" : requestDTO.getAddrDetail().trim();
                        String query = (base + " " + detail).trim().replaceAll("\\s+", " ");

                        GeocodingResult[] results = GeocodingApi.newRequest(geoApiContext)
                                .address(query)
                                .region("kr")    // 한국 주소 힌트 (선택)
                                .language("ko")  // 한국어 응답 (선택)
                                .await();

                        if (results == null || results.length == 0) {
                            throw new AddressHandler(ErrorStatus.ADDRESS_NOT_FOUND);
                        }

                        Double lat = results[0].geometry.location.lat;
                        Double lng = results[0].geometry.location.lng;

                        Address newAddress = Address.builder()
                                .addr(requestDTO.getAddr())
                                .addrDetail(requestDTO.getAddrDetail())
                                .lat(lat)
                                .lng(lng)
                                .count(1L)
                                .build();

                        return addressRepository.save(newAddress);

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AddressHandler(ErrorStatus.GEOCODING_FAILED);
                    } catch (Exception e) {
                        throw new AddressHandler(ErrorStatus.GEOCODING_FAILED);
                    }
                });

        // DB에서 가져온 user와 address 정보로 이미 신고한 내역이 있는지 확인
        if (reportRepository.existsByUserAndAddress(user, address)) {
            throw new ReportHandler(ErrorStatus.REPORT_ALREADY_EXISTS);
        }

        // 파일 업데이트 함수 호출(fileKey 생성)
        String fileKey = s3Service.uploadPdf(file);

        Report newReport = Report.builder()
                .registrationStatus(RegistrationStatus.PENDING)
                .content(requestDTO.getContent())
                .contractedAt(requestDTO.getContractedAt())
                .recognitionAt(requestDTO.getRecognitionAt())
                .documentUrl(fileKey)
                .user(user)
                .address(address)
                .contractType(contractType)
                .classification(classification)
                .build();

        Report savedReport = reportRepository.save(newReport);
        alarmService.notifyReportSubmitted(savedReport);
        return savedReport;
    }

    // 사용자 신고글 삭제
    @Override
    @Transactional
    public void deleteReport(Long userId, Long reportId){
        User user = verifyUser(userId);

        Report report = reportRepository.findByIdAndUserId(reportId, userId).orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_NOT_FOUND));

        // 파일 삭제
        s3Service.deletePdf(report.getDocumentUrl());

        // 삭제할 신고글과 연결된 주소 엔티티를 미리 가져옴
        Address address = report.getAddress();

        // 신고글 삭제
        reportRepository.delete(report);

        address.setCount(address.getCount() - 1);

        // 주소 엔티티를 사용하는 신고글이 없다면 주소 엔티티도 삭제
        if (address.getCount() <= 0) {
            addressRepository.delete(address);
        }
    }


    // 사기 등록 상태 변경(관리자-거절/수락)
    @Override
    @Transactional
    public ReportResponseDTO.ChageStatusOfReportDTO changeStatusOfReport(Long userId, Long reportId, ReportRequestDTO.ChangeStatusRequestDTO request){
        // 권한 없음 예외 처리
        if (verifyUser(userId).getRole() != Role.ADMIN) {
            throw new UserHandler(ErrorStatus.USER_FORBIDDEN);
        }

        // 신고글 조회
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportHandler(ErrorStatus.REPORT_NOT_FOUND));

        // 변경하려는 신고글의 상태가 PENDING인지 확인
        if (report.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new ReportHandler(ErrorStatus.REPORT_NOT_PENDING);
        }

        String status = request.getChangeStatus().toUpperCase();

        // 변경할 상태가 APPROVED 또는 REJECTED인지 확인
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new ReportHandler(ErrorStatus.INVALID_STATUS_CHANGE);
        }

        RegistrationStatus newStatus = RegistrationStatus.valueOf(status);

        // 상태 변경
        if (newStatus == RegistrationStatus.REJECTED) {
            String reason = request.getRejectReason();
            if (reason == null || reason.isBlank() || reason.equals("string")) {
                throw new ReportHandler(ErrorStatus.REJECT_REASON_NOT_FOUND);
            }
            report.setRejectReason(reason);
        }

        report.setRegistrationStatus(newStatus);

        if (newStatus == RegistrationStatus.REJECTED) {
            alarmService.notifyReportRejected(report, userId);
        }

        return ReportConverter.toChangeStatusOfReportDTO(report);
    }

    // 회원 검증
    User verifyUser(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
    }
}
