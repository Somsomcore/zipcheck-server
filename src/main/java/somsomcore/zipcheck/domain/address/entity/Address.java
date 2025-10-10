package somsomcore.zipcheck.domain.address.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import somsomcore.zipcheck.global.common.BaseEntity;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주소
    @Column(nullable = false)
    private String addr;

    // 상세주소
    @Column(nullable = false)
    private String addrDetail;

    // 위도
    @Column(nullable = false)
    private Double lat;

    // 경도
    @Column(nullable = false)
    private Double lng;

    // 누적 횟수
    @Column(nullable = false)
    @ColumnDefault("0")
    private long count;
}
