package somsomcore.zipcheck.domain.risk.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono; // 비동기 처리를 위해 Mono 사용 (선택)
import somsomcore.zipcheck.domain.risk.dto.api.ApiResponseDto;
import somsomcore.zipcheck.domain.risk.dto.api.BodyDto;
import somsomcore.zipcheck.domain.risk.dto.api.HeaderDto;
import somsomcore.zipcheck.domain.risk.dto.api.ItemDto;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j // 로그 사용을 위한 어노테이션
@Component
@RequiredArgsConstructor
public class ExternalApiClient {

    private final WebClient webClient; // AppConfig에서 설정한 WebClient 주입

    @Value("${api.serviceKey}") // application.yml에서 서비스 키 주입
    private String serviceKey;

    private static final int NUM_OF_ROWS = 1000; // 한 페이지에 최대로 가져올 데이터 수 (API 명세 확인 필요!)

    /**
     * 특정 지역 코드와 계약년월에 해당하는 전월세 데이터를 API에서 가져옵니다.
     * 페이지네이션을 처리하여 해당 월의 모든 데이터를 가져옵니다.
     * @param regionCode 법정동 코드 앞 5자리
     * @param yearMonth 계약년월 (YYYYMM)
     * @return 해당 월의 전체 ItemDto 리스트
     */
    public List<ItemDto> fetchMonthlyRentData(String regionCode, String yearMonth, String propertyType) {

        String apiPath;
        switch (propertyType) {
            case "아파트":
                apiPath = "/RTMSDataSvcAptRent/getRTMSDataSvcAptRent";
                break;
            case "연립다세대":
                apiPath = "/RTMSDataSvcRHRent/getRTMSDataSvcRHRent";
                break;
            case "오피스텔":
                apiPath = "/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent";
                break;
            case "다가구":
            case "단독":
                apiPath = "/RTMSDataSvcSHRent/getRTMSDataSvcSHRent";
                break;
            default:
                log.warn("Unknown property type: {}", propertyType);
                return Collections.emptyList(); // 빈 리스트 반환
        }

        log.info("Fetching rent data for type: {}, region: {}, month: {}", propertyType, regionCode, yearMonth);

        List<ItemDto> totalItems = new ArrayList<>();
        int pageNo = 1;
        int totalCount = 0;

        while (true) {
            final int currentPageNo = pageNo;
            log.debug("Fetching page: {}", currentPageNo);

            try {
                ApiResponseDto response = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(apiPath) // 2. ⭐️ 선택된 API 경로 사용
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("LAWD_CD", regionCode)
                                .queryParam("DEAL_YMD", yearMonth)
                                .queryParam("pageNo", currentPageNo)
                                .queryParam("numOfRows", NUM_OF_ROWS)
                                .build())
                        .retrieve()
                        .bodyToMono(ApiResponseDto.class)
                        .block();

                // --- 👇 로그 추가 부분 ---
                if (response != null && response.getHeader() != null) {
                    HeaderDto header = response.getHeader();
                    log.info("API Response Header - Result Code: {}, Result Message: {}",
                            header.getResultCode(), header.getResultMsg());
                } else {
                    log.error("API call failed for page {}: Response or Header is null", currentPageNo);
                    break;
                }

                // --- 오류 조건 수정 끝 ---

                // 응답 유효성 검사
                if (response == null || response.getHeader() == null || !"000".equals(response.getHeader().getResultCode())) {
                    log.error("API call failed for page {}: {}", currentPageNo, response != null && response.getHeader() != null ? response.getHeader().getResultMsg() : "Unknown error");
                    break; // 에러 발생 시 반복 중단
                }

                BodyDto body = response.getBody();
                if (body == null || body.getItems() == null) {
                    log.warn("No items found in response body for page {}", currentPageNo);
                    break; // 응답 본문이나 items가 없으면 중단
                }

                // 첫 페이지에서만 totalCount 설정
                if (currentPageNo == 1) {
                    totalCount = body.getTotalCount();
                    log.info("Total items for {}/{}: {}", regionCode, yearMonth, totalCount);
                    if (totalCount == 0) {
                        break;
                    }
                }

                List<ItemDto> itemsOnPage = body.getItems().getItem();
                if (itemsOnPage.isEmpty()) {
                    log.debug("No more items found on page {}", currentPageNo);
                    break; // 현재 페이지에 아이템이 없으면 종료
                }

                totalItems.addAll(itemsOnPage);
                log.debug("Fetched {} items from page {}. Total fetched: {}", itemsOnPage.size(), currentPageNo, totalItems.size());

                // 모든 데이터를 가져왔는지 확인
                if (totalItems.size() >= totalCount) {
                    log.info("Successfully fetched all {} items.", totalItems.size());
                    break; // 가져온 아이템 수가 전체 수와 같거나 많으면 종료
                }

                pageNo++; // 다음 페이지로

            } catch (Exception e) {
                log.error("Error during API call for page {}: {}", currentPageNo, e.getMessage(), e);
                // 필요하다면 재시도 로직 추가 가능
                break; // 예외 발생 시 반복 중단
            }
        } // end while

        return totalItems;
    }
}
