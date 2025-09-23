package somsomcore.zipcheck.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import somsomcore.zipcheck.domain.user.entity.User;

@Getter
@Builder
public class UserProfileResponseDto {

    private String name;
    private String profileUrl;
    private String oauthType;
    private String email;

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .oauthType(user.getOauthType().name())
                .email(user.getEmail())
                .build();
    }
}