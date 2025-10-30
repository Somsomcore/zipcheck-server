package somsomcore.zipcheck.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.global.common.BaseEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이름
    @Column(nullable = false)
    private String name;

    // 역할
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'MEMBER'")
    private Role role;

    // 이메일
    @Column(nullable = false, unique = true)
    private String email;

    // 소셜 로그인 타입
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'KAKAO'")
    private OauthType oauthType;

    // 전화번호
    @Column(nullable = false)
    private String phone;

	// 전화번호 인증 코드 (추후 Redis 교체 예정)
	@Column
	private String phoneValidationCode;
	
	// 전화번호 인증 만료 일자
	@Column
	private LocalDateTime phoneValidationExpiresAt;

    // 전화번호 인증 여부
    @Column(nullable = false)
    private boolean isVerified;

    // 프로필 이미지 URL
    @Column
    private String profileUrl;

	public void updatePhoneValidation(String code, LocalDateTime expiresAt) {
		this.phoneValidationCode = code;
		this.phoneValidationExpiresAt = expiresAt;
	}
}
