package somsomcore.zipcheck.domain.risk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import somsomcore.zipcheck.domain.risk.converter.RiskConverter;
import somsomcore.zipcheck.domain.risk.dto.RiskResponseDTO;
import somsomcore.zipcheck.domain.risk.entity.Risk;
import somsomcore.zipcheck.domain.risk.repository.RiskRepository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskQueryServiceImpl implements RiskQueryService{


    private final RiskRepository riskRepository;
    private final UserRepository userRepository;

    @Override
    public RiskResponseDTO.RiskListDTO getRiskListByUser(Long userId, Integer year, Integer month, Pageable pageable) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        Page<Risk> riskPage;

        if (year != null && month != null) {

            LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endDateTime = startDateTime.plusMonths(1);

            riskPage = riskRepository.findByUserAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    user, startDateTime, endDateTime, pageable
            );

        } else {
             riskPage = riskRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        return RiskConverter.riskListDTO(riskPage);
    }



}
