package somsomcore.zipcheck.domain.report.repository.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.report.entity.Report;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * Report의 id와 해당 Report를 작성한 User의 id를 기준으로 신고글을 조회합니다.
     * @param id 조회할 Report의 id
     * @param userId 해당 Report를 작성한 User의 id
     * @return Optional<Report>
     */
    Optional<Report> findByIdAndUserId(Long id, Long userId);

    /**
     * 특정 사용자의 신고글을 최신순으로 페이징 조회합니다.
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return Page<Report>
     */
    @Query("SELECT r FROM Report r " +
           "JOIN FETCH r.address a " +
           "JOIN FETCH r.contractType ct " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.createdAt DESC")
    Page<Report> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);
}
