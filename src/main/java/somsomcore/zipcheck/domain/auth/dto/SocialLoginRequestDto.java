package somsomcore.zipcheck.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;

@Getter
@NoArgsConstructor
public class SocialLoginRequestDto {

    @NotNull(message = "OAuth 타입은 필수입니다.")
    private OauthType provider;

    @NotBlank(message = "액세스 토큰은 필수입니다.")
    private String accessToken;
}