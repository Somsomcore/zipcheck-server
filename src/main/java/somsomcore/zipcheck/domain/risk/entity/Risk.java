package somsomcore.zipcheck.domain.risk.entity;

import jakarta.persistence.*;
import lombok.*;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.risk.entity.enums.RiskLevel;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.global.common.BaseEntity;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Risk extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 위험도
    @Column(nullable = false)
    private Double riskScore;

    // 위험도 레벨
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'Critical'")
    private RiskLevel riskLevel;

    // 종합 퍼센트
    @Column(nullable = false)
    private Double depositPct;

    // 최우선 변제금액
    @Column(nullable = false)
    private Long maxPra;

    // 최우선 변제액
    @Column(nullable = false)
    private Long pra;

    // 동면적 매물 보증금
    @Column(nullable = false)
    private Long similarDeposit;

    // 동면적 매물 대비 퍼센트
    @Column(nullable = false)
    private Double similarDepositPct;

    // 보증금 평균
    @Column(nullable = false)
    private Long average;

    // 보증금 중앙값
    @Column(nullable = false)
    private Long median;

    // 보증금 최저가
    @Column(nullable = false)
    private Long minimum;

    // 보증금 최고가
    @Column(nullable = false)
    private Long maximum;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 주소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addr_id")
    private Address address;



}
