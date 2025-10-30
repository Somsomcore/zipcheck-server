package somsomcore.zipcheck.domain.risk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.risk.entity.Risk;
import somsomcore.zipcheck.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long> {
    /**
     * 사용자의 가장 최근 Risk 기록을 생성일(createdAt) 내림차순으로 1건 조회합니다.
     * @param user 조회할 사용자
     * @return 가장 최근의 Risk 기록
     */
    Optional<Risk> findTopByUserOrderByCreatedAtDesc(User user);


    Page<Risk> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Risk> findByUserAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            User user,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
