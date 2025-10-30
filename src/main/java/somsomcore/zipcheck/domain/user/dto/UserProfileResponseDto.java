package somsomcore.zipcheck.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;

@Getter
@Builder
public class UserProfileResponseDto {

    private String name;
    private String profileUrl;
    private String oauthType;
    private String email;
	private Role role;

    public static UserProfileResponseDto from(User user) {
        return UserProfileResponseDto.builder()
                .name(user.getName())
                .profileUrl(user.getProfileUrl())
                .oauthType(user.getOauthType().name())
                .email(user.getEmail())
				.role(user.getRole())
                .build();
    }
}