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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.DoubleSummaryStatistics;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealEstateService {

    private final ExternalApiClient externalApiClient;
    private final AddressRepository addressRepository;
    private final RiskRepository riskRepository;
    private final GeoApiContext geoApiContext;

    @Transactional
    public RiskResponseDTO.RiskDetailDTO analyzeAndSaveRisk(String regionCode, RentFilterRequestDto filterDto, User user) {
        LocalDate today = LocalDate.now();
        List<String> yearMonths = List.of(
                today.format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM")),
                today.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM"))
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
                    // 아파트, 오피스텔은 이 필터가 필요 없음
                    if ("아파트".equals(propertyType) || "오피스텔".equals(propertyType)|| "연립다세대".equals(propertyType)) {
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
                percentDifference = ((filterDto.getDeposit() - averageRent) / averageRent) * 100;
            }
        }

        Double riskScore = calculateRiskScore(filterDto.getDeposit(), averageRent, maxRent); // 예시
        RiskLevel riskLevel = calculateRiskLevel(riskScore); // 예시
        Long maxPra = 150000000L;
        Long pra = 50000000L;

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
    private Double calculateRiskScore(Double userDeposit, double average, double max) {
        // ... (실제 위험도 계산 로직 구현) ...
        return 85.5; // 예시 값
    }
    private RiskLevel calculateRiskLevel(Double score) {
        // ... (점수에 따른 위험 레벨 반환 로직 구현) ...
        return RiskLevel.Critical; // 예시 값
    }
}