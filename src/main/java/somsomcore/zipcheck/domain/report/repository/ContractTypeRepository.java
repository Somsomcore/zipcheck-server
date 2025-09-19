package somsomcore.zipcheck.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.report.entity.ContractType;

@Repository
public interface ContractTypeRepository extends JpaRepository<ContractType, Long> {
}
