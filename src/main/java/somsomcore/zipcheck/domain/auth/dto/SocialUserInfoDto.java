package somsomcore.zipcheck.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfoDto {

    private String email;
    private String name;
    private OauthType provider;
    private String providerId;
}