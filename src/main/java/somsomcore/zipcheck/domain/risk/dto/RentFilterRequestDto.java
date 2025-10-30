package somsomcore.zipcheck.domain.risk.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentFilterRequestDto {

    @NotNull(message = "거래 희망 보증금을 입력해주세요.")
    @Positive(message = "보증금은 0보다 커야 합니다.")
    private Double deposit;

    @NotBlank(message = "매물 종류를 입력해주세요.")
    private String propertyType;

    @NotNull(message = "전용 면적을 입력해주세요.")
    @Positive(message = "전용 면적은 0보다 커야 합니다.")
    private Double area;

    @Positive(message = "층수는 0보다 커야 합니다.")
    @Nullable
    private Integer floor; // 층수 (선택)

    @Positive(message = "건축년도는 0보다 커야 합니다.")
    @Nullable
    private Integer buildYear; // 건축년도 (선택)

    @NotNull(message = "주소를 입력해주세요.")
    private String address;

    @NotNull(message = "상세 주소를 입력해주세요.")
    private String addressDetail;
}