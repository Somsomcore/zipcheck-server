package somsomcore.zipcheck.domain.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import somsomcore.zipcheck.domain.risk.entity.enums.RiskLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RiskResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskListDTO {

        List<DailyRiskListDTO> dailyRiskList;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRiskListDTO {

        private LocalDate date; // 👈 해당 날짜 (예: "2025-10-30")
        private List<RiskDetailDTO> risks; // 👈 그 날짜에 해당하는 위험도 리스트
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskDetailDTO {
        Long riskId;
        Long userId;
        Double riskScore;
        RiskLevel riskLevel;
        Double depositPct;
        Long average;
        Long minimum;
        Long maximum;
        Long standardDeviation;
        LocalDateTime createdAt;
        String address;
        String addressDetail;
    }


}
