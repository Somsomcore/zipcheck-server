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
                  "SELECT a.lat as latitude, a.lng as longitude, a.addr as address, " +
                  // 1번 수정: COUNT(r.report_id) -> COUNT(*)
                  "COUNT(*) as reportCount " +
                  "FROM address a " +
                  // 2번 수정: a.addr_id -> a.id
                  "JOIN report r ON a.id = r.addr_id " +
                  "WHERE ST_Distance_Sphere(POINT(:lng, :lat), POINT(a.lng, a.lat)) <= :radiusMeters " +
                  "GROUP BY a.id", // GROUP BY 기준도 a.addr_id에서 a.id로 변경해주는 것이 더 명확합니다.
          nativeQuery = true)
    List<ReportAddressCount> findAddressesInRadiusWithReportCount(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") int radiusMeters);
}
