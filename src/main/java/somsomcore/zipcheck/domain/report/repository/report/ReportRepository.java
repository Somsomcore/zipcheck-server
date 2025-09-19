package somsomcore.zipcheck.domain.report.repository.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.report.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}
