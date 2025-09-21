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
import somsomcore.zipcheck.domain.report.dto.report.ReportRequestDTO;
import somsomcore.zipcheck.domain.report.entity.Classification;
import somsomcore.zipcheck.domain.report.entity.ContractType;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;
import somsomcore.zipcheck.domain.report.repository.ClassificationRepository;
import somsomcore.zipcheck.domain.report.repository.ContractTypeRepository;
import somsomcore.zipcheck.domain.report.repository.report.ReportRepository;
import somsomcore.zipcheck.domain.s3.service.S3Service;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.repository.UserRepository;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.handler.AddressHandler;
import somsomcore.zipcheck.global.apiPayload.exception.handler.ContractTypeHandler;
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

    // 사용자 사기 접수
    @Override
    @Transactional
    public Report addReport(Long memberId, ReportRequestDTO.addRequestReportDTO requestDTO, MultipartFile file){
        // 실제 회원인지 검증
        User user = userRepository.findById(memberId).orElseThrow(() -> {
            throw new UserHandler(ErrorStatus.USER_NOT_FOUND);
        });

        // 계약 형태
        ContractType contractType = contractTypeRepository.findById(requestDTO.getContractType()).orElseThrow(() -> {
            throw new ContractTypeHandler(ErrorStatus.CONTRACTTYPE_NOT_FOUND);
        });

        // 사기 분류
        Classification classification = classificationRepository.findById(requestDTO.getClassification()).orElseThrow(() -> {
            throw new ContractTypeHandler(ErrorStatus.CLASSIFICATION_NOT_FOUND);
        });

        // 주소 처리: DB에 없으면 지오코딩으로 lat/lng 조회 후 저장
        Address address = addressRepository
                .findByAddrAndAddrDetail(requestDTO.getAddr(), requestDTO.getAddrDetail())
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
                                .build();

                        return addressRepository.save(newAddress);

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AddressHandler(ErrorStatus.GEOCODING_FAILED);
                    } catch (Exception e) {
                        throw new AddressHandler(ErrorStatus.GEOCODING_FAILED);
                    }
                });

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

        return reportRepository.save(newReport);
    }
}
