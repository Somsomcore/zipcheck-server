package somsomcore.zipcheck.domain.alarm.entity;

import jakarta.persistence.*;
import lombok.*;
import somsomcore.zipcheck.domain.alarm.entity.enums.AlarmType;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.global.common.BaseEntity;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용
    @Column(nullable = false, length = 255)
    private String content;

    // 알림 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlarmType type;

    // 수신 여부
    @Builder.Default
    @Column(nullable = false)
    private Boolean isConfirmed = Boolean.FALSE;

    // 수신자 ID
    @Column(nullable = false)
    private Long receiverId;

    // 송신자 ID
    @Column(nullable = false)
    private Long senderId;

    // 신고글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    public void markConfirmed() {
        this.isConfirmed = Boolean.TRUE;
    }
}
