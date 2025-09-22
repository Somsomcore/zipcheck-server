package somsomcore.zipcheck.domain.report.repository.report;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
