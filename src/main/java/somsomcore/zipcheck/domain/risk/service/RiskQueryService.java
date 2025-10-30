package somsomcore.zipcheck.domain.risk.service;


import org.springframework.data.domain.Pageable;
import somsomcore.zipcheck.domain.risk.dto.RiskResponseDTO;


public interface RiskQueryService {

   RiskResponseDTO.RiskListDTO getRiskListByUser(Long userId, Integer year, Integer month, Pageable pageable);

}
