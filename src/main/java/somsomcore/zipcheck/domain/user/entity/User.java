package somsomcore.zipcheck.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.global.common.BaseEntity;

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
    @Column(nullable = false)
    private String email;

    // 소셜 로그인 타입
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'KAKAO'")
    private OauthType oauthType;

    // 전화번호
    @Column(nullable = false)
    private String phone;

    // 전화번호 인증 여부
    @Column(nullable = false)
    private boolean isVerified;


}
