package somsomcore.zipcheck.domain.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AverageResultDto {
    private double averageRent; // 평균 보증금 (만원)
    private double maxRent;     // 최고 보증금 (만원)
    private double minRent;     // 최저 보증금 (만원)
    private double standardDeviation; // 표준편차(만원)
    private double percentDifference; // 보증금과의 퍼센트 차이
}
