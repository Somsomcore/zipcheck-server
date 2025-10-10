package somsomcore.zipcheck.domain.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.report.repository.ReportAddressCount;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // 주소+상세주소로 단일 조회 (정규화된 문자열 기준)
    Optional<Address> findByAddrAndAddrDetail(String addr, String addrDetail);

    @Query(value =
            "SELECT " +
                    "    ANY_VALUE(a.lat) as latitude, " + // 그룹 내 임의의 lat 값 선택
                    "    ANY_VALUE(a.lng) as longitude, " +// 그룹 내 임의의 lng 값 선택
                    "    a.addr as address, " +
                    "    COUNT(r.id) as reportCount " +  // count 값을 합산
                    "FROM address a " +
                    "JOIN report r ON a.id = r.addr_id " +
                    "WHERE ST_Distance_Sphere(POINT(:lng, :lat), POINT(a.lng, a.lat)) <= :radiusMeters " +
                    "AND r.registration_status = 'APPROVED' " +
                    "GROUP BY a.addr", // 기본 주소(addr)를 기준으로 그룹화
            nativeQuery = true)
    List<ReportAddressCount> findGroupedAddressesInRadius(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") int radiusMeters);
}
