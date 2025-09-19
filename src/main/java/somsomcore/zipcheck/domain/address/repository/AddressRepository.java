package somsomcore.zipcheck.domain.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.address.entity.Address;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // 주소+상세주소로 단일 조회 (정규화된 문자열 기준)
    Optional<Address> findByAddrAndAddrDetail(String addr, String addrDetail);
}
