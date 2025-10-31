package somsomcore.zipcheck.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestTokenRequestDto {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
}
