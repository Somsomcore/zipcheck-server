package somsomcore.zipcheck.domain.risk.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.address.repository.AddressRepository;
import somsomcore.zipcheck.domain.risk.client.ExternalApiClient;
import somsomcore.zipcheck.domain.risk.converter.RiskConverter;
import somsomcore.zipcheck.domain.risk.dto.RentFilterRequestDto;
import somsomcore.zipcheck.domain.risk.dto.RiskResponseDTO;
import somsomcore.zipcheck.domain.risk.dto.api.ItemDto;
import somsomcore.zipcheck.domain.risk.entity.Risk;
import somsomcore.zipcheck.domain.risk.entity.enums.RiskLevel;
import somsomcore.zipcheck.domain.risk.repository.RiskRepository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.global.apiPayload.code.status.ErrorStatus;
import somsomcore.zipcheck.global.apiPayload.exception.handler.AddressHandler;
import somsomcore.zipcheck.global.config.PolicyProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealEstateService {

    private final ExternalApiClient externalApiClient;
    private final AddressRepository addressRepository;
    private final RiskRepository riskRepository;
    private final GeoApiContext geoApiContext;
    private final PolicyProperties policyProperties;

    @Transactional
    public RiskResponseDTO.RiskDetailDTO analyzeAndSaveRisk(String regionCode, RentFilterRequestDto filterDto, User user) {
        LocalDate today = LocalDate.now();
        List<String> yearMonths = List.of(
                today.format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(3).format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(4).format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(5).format(DateTimeFormatter.ofPattern("yyyyMM"))
        );

        log.info("Calculating average rent for region: {}, filters: {}", regionCode, filterDto);

        String propertyType = filterDto.getPropertyType();
        log.info("Calculating average rent for type: {}, region: {}, months: {}", propertyType, regionCode, yearMonths);

        List<ItemDto> allItems = new ArrayList<>();
        for (String ym : yearMonths) {
            List<ItemDto> monthlyItems = externalApiClient.fetchMonthlyRentData(regionCode, ym, propertyType);
            allItems.addAll(monthlyItems);
        }

        List<Double> validDeposits = allItems.stream()
                // 1. 월세가 0인 전세 계약만 필터링
                .filter(item -> {
                    try {
                        return Integer.parseInt(item.getMonthlyRentAmount().trim()) == 0;
                    } catch (NumberFormatException | NullPointerException e) {
                        return false;
                    }
                })

                .filter(item -> {
                    // 아파트, 오피스텔, 연립다세대, 단독, 다가구는 이미 파라미터별로 API가 분리 호출되므로 houseType 검사를 면제
                    if ("아파트".equals(propertyType) || "오피스텔".equals(propertyType) || "연립다세대".equals(propertyType) || "단독".equals(propertyType) || "다가구".equals(propertyType)) {
                        return true;
                    }
                    return propertyType.equals(item.getHouseType());
                })

                .filter(item -> {

                    double requestedArea = filterDto.getArea();
                    Double itemArea = 0.0; // 검사할 면적 (m^2)

                    if ("아파트".equals(propertyType) || "오피스텔".equals(propertyType) ||"연립다세대".equals(propertyType)) {
                        itemArea = item.getDedicatedArea();
                    } else {
                        itemArea = item.getTotalFloorAr();
                    }

                    if (itemArea == null || itemArea == 0.0) return false;

                    return itemArea >= (requestedArea * 0.9) && itemArea <= (requestedArea * 1.1);
                })

                .filter(item -> filterDto.getFloor() == null || item.getFloor() >= (filterDto.getFloor()- 5) && item.getFloor() <= (filterDto.getFloor() + 5))

                .filter(item -> filterDto.getBuildYear() == null || item.getBuildYear() >= (filterDto.getBuildYear()- 5) && item.getBuildYear() <= (filterDto.getBuildYear() + 5))

                .map(this::parseDepositAmountToDouble)
                .filter(Objects::nonNull)
                .toList();

        log.info("Items after filtering: {}", validDeposits.size());

        DoubleSummaryStatistics stats = validDeposits.stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        double averageRent = stats.getAverage();
        double maxRent = stats.getMax();
        double minRent = stats.getMin();
        double variance = 0.0;
        double standardDeviation = 0.0;
        double percentDifference = 0.0;
        long count = stats.getCount();

        if (count > 0) {
            double sumOfSquaredDifferences = validDeposits.stream()
                    .mapToDouble(Double::doubleValue)
                    .map(deposit -> Math.pow(deposit - averageRent, 2))
                    .sum();

            variance = sumOfSquaredDifferences / count;
            standardDeviation = Math.sqrt(variance);

            if (filterDto.getDeposit() != null && averageRent > 0) {
                // 공식: ((요청값 - 평균값) / 평균값) * 100
                double rawPercentDiff = ((filterDto.getDeposit() - averageRent) / averageRent) * 100;

                percentDifference = Math.round(rawPercentDiff * 100.0) / 100.0;
            }
        }


        Double riskScore = calculateRiskScore(percentDifference);
        RiskLevel riskLevel = calculateRiskLevel(riskScore);

        double finalAverage = (count == 0) ? 0.0 : averageRent;
        double finalMax = (count == 0) ? 0.0 : maxRent;
        double finalMin = (count == 0) ? 0.0 : minRent;

        log.info("Calculated Average Deposit: {}", finalAverage);
        log.info("Calculated Max Deposit: {}", finalMax);
        log.info("Calculated Min Deposit: {}", finalMin);
        log.info("Calculated Variance: {}", variance);
        log.info("Calculated StandardDeviation: {}", standardDeviation);
        log.info("Calculated Percent Difference: {}%", percentDifference);

        Address address = addressRepository
                .findByAddrAndAddrDetail(filterDto.getAddress(), filterDto.getAddressDetail())
                .map(existingAddress -> {
                    existingAddress.setCount(existingAddress.getCount() + 1);
                    return existingAddress;
                })
                .orElseGet(() -> {
                    try {
                        String base = filterDto.getAddress() == null ? "" : filterDto.getAddress().trim();
                        String detail = filterDto.getAddressDetail() == null ? "" : filterDto.getAddressDetail().trim();
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
                                .addr(filterDto.getAddress())
                                .addrDetail(filterDto.getAddressDetail())
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

        long userDeposit = filterDto.getDeposit() != null ? filterDto.getDeposit().longValue() : 0L;
        PolicyProperties.RegionRule rule = findRuleForAddress(filterDto.getAddress());
        Long pra = getPriorityRepaymentAmount(rule, userDeposit);
        Long maxPra = rule.getRepaymentAmount();


        Risk newRisk = Risk.builder()
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .depositPct(percentDifference)
                .maxPra(maxPra)
                .pra(pra)
                .average((long) finalAverage)
                .minimum((long) finalMin)
                .maximum((long) finalMax)
                .standardDeviation((long) standardDeviation)
                .user(user)
                .address(address)
                .build();

        Risk savedRisk = riskRepository.save(newRisk);

        return RiskConverter.riskDetailDTO(savedRisk);
    }



    private Double parseDepositAmountToDouble(ItemDto item) {
        String depositStr = item.getDepositAmount();
        if (depositStr == null || depositStr.isBlank()) {
            return null;
        }

        try {
            depositStr = depositStr.trim().replace(",", "");
            double depositValue = Double.parseDouble(depositStr);

            return depositValue;

        } catch (NumberFormatException e) {
            log.warn("Failed to parse deposit amount: '{}' for item: {}", item.getDepositAmount(), item);
            return null; // 파싱 실패 시 null 반환
        }

    }
    private Double calculateRiskScore(double percentDifference) {

        double absPercentDifference = Math.abs(percentDifference);

        double riskScore = absPercentDifference * 2.0;

        double finalScore = Math.min(riskScore, 100.0);


        return Math.round(finalScore * 100.0) / 100.0;
    }
    private RiskLevel calculateRiskLevel(Double score) {


        if (score >= 100.0) {
            return RiskLevel.Critical;
        }

        else if (score >= 60.0) {
            return RiskLevel.Danger;
        }

        else {
            return RiskLevel.Caution;
        }

    }

    /**
     * 주소와 보증금을 기준으로 최우선변제액을 반환합니다.
     * @param rule 판별된 지역 규칙
     * @param userDeposit 사용자의 보증금
     * @return 최우선변제액 (대상 아니면 0L)
     */
    public long getPriorityRepaymentAmount(PolicyProperties.RegionRule rule, long userDeposit) {
        if (userDeposit <= rule.getDepositLimit()) {
            return rule.getRepaymentAmount();
        } else {
            return 0L;
        }
    }

    /**
     * 주소 문자열을 파싱하여 올바른 지역 규칙(Rule)을 찾습니다.
     * @param address "서울 강남구...", "경기 광주시..."
     * @return 지역에 맞는 RegionRule 객체
     */
    private PolicyProperties.RegionRule findRuleForAddress(String address) {
        Map<String, PolicyProperties.RegionRule> rules = policyProperties.getRepaymentRules();

        if (address == null || address.isBlank()) {
            return rules.get("OTHER");
        }

        // 1호: 서울특별시
        // "서울"은 항상 주소 맨 앞에 오므로 startsWith 사용이 가장 효율적입니다.
        if (address.startsWith("서울")) {
            return rules.get("SEOUL");
        }

        // 2호: 과밀억제권역 (인천, 세종, 용인, 화성, 김포)
        // "인천", "세종"은 특별시/광역시급이라 startsWith로 검사합니다.
        if (address.startsWith("인천") || // "인천광역시"
                address.startsWith("세종")) { // "세종특별자치시"
            return rules.get("OVERCROWDED");
        }

        // "경기도 용인시", "경기도 화성시", "경기도 김포시" 등은 contains로 검사합니다.
        if (address.contains("용인시") ||
                address.contains("화성시") ||
                address.contains("김포시")) {
            return rules.get("OVERCROWDED");
        }

        // 3호: 광역시 등 (인천 제외, 경기도 광주 포함)

        // "광주시"는 "광주광역시"와 "경기도 광주시"를 모두 포함하며,
        // 표 기준(3호)으로 둘 다 동일하게 취급하므로 contains("광주시")가 올바른 검사입니다.
        if (address.contains("안산시") ||
                address.contains("광주시") || // "광주광역시" OR "경기도 광주시"
                address.contains("파주시") ||
                address.contains("이천시") ||
                address.contains("평택시")) {
            return rules.get("METROPOLITAN");
        }

        // 2호(인천), 3호(광주)를 제외한 나머지 광역시(대전, 대구, 울산, 부산)를 검사합니다.
        if (address.contains("광역시") && !address.startsWith("인천") && !address.startsWith("광주")) {
            return rules.get("METROPOLITAN");
        }

        // 4호: 그 밖의 지역
        return rules.get("OTHER");
    }

}