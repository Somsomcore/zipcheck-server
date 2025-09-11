package somsomcore.zipcheck.domain.report.entity;

import jakarta.persistence.*;
import lombok.*;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.global.common.BaseEntity;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관리자 승인 여부
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'PENDING'")
    private RegistrationStatus registrationStatus;

    // 내용
    @Column(nullable = false)
    private String rejectReason;

    // 내용
    @Column(nullable = false)
    private String content;

    // 계약 일자
    @Column(nullable = false)
    private Date contractedAt;

    // 사기 인지 일자
    @Column(nullable = false)
    private Date recognitionAt;

    // 신고글 근거 자료
    @Column(nullable = false)
    private String documentUrl;

    // 신고글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 주소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addr_id")
    private Address address;

    // 계약 형태
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractType_id")
    private ContractType contractType;

    // 사기 분류
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_id")
    private Classification classification;



}
