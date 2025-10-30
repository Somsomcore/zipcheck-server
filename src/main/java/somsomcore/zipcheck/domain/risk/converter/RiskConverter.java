package somsomcore.zipcheck.domain.risk.converter;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import somsomcore.zipcheck.domain.risk.dto.RiskResponseDTO;
import somsomcore.zipcheck.domain.risk.entity.Risk;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RiskConverter {

    public static RiskResponseDTO.RiskDetailDTO riskDetailDTO(Risk risk) {
        return RiskResponseDTO.RiskDetailDTO.builder()
                .userId(risk.getUser().getId())
                .riskId(risk.getId())
                .average(risk.getAverage())
                .minimum(risk.getMinimum())
                .maximum(risk.getMaximum())
                .depositPct(risk.getDepositPct())
                .standardDeviation(risk.getStandardDeviation())
                .riskLevel(risk.getRiskLevel())
                .riskScore(risk.getRiskScore())
                .address(risk.getAddress().getAddr())
                .addressDetail(risk.getAddress().getAddrDetail())
                .createdAt(risk.getCreatedAt())
                .build();
    }


    public static RiskResponseDTO.RiskListDTO riskListDTO(Page<Risk> riskPage) {

        // 1. 현재 페이지의 Risk 엔티티 목록을 RiskDetailDTO 목록으로 변환
        List<RiskResponseDTO.RiskDetailDTO> riskDetailList = riskPage.getContent().stream()
                .map(RiskConverter::riskDetailDTO)
                .collect(Collectors.toList());

        // 2. DTO 목록을 날짜(LocalDate) 기준으로 그룹핑 (Map 생성)
        Map<LocalDate, List<RiskResponseDTO.RiskDetailDTO>> groupedByDateMap = riskDetailList.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 3.  Map을 List<DailyRiskListDto>로 변환
        List<RiskResponseDTO.DailyRiskListDTO> dailyList = groupedByDateMap.entrySet().stream()
                .map(entry -> RiskResponseDTO.DailyRiskListDTO.builder()
                        .date(entry.getKey())
                        .risks(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 4. 최종 페이지 정보와 날짜별로 묶인 리스트(dailyList)를 담아 반환
        return RiskResponseDTO.RiskListDTO.builder()
                .isFirst(riskPage.isFirst())
                .isLast(riskPage.isLast())
                .totalPage(riskPage.getTotalPages())
                .totalElements(riskPage.getTotalElements())
                .listSize(riskDetailList.size())
                .dailyRiskList(dailyList)
                .build();
    }


}
