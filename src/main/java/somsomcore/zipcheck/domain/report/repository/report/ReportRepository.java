package somsomcore.zipcheck.domain.report.repository.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.address.entity.Address;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.report.entity.enums.RegistrationStatus;
import somsomcore.zipcheck.domain.report.repository.ReportWithAddressCount;
import somsomcore.zipcheck.domain.user.entity.User;

import java.util.List;
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

    /**
     * 특정 주소 문자열을 포함하고, 등록 상태가 일치하는 신고글 목록을 페이징하여 조회합니다.
     * @param addr 검색할 주소 문자열
     * @param status 조회할 등록 상태 (예: APPROVED)
     * @param pageable 페이징 정보
     * @return 페이징된 신고글 목록
     */
    Page<Report> findAllByAddressAddrContainingAndRegistrationStatus(String addr, RegistrationStatus status, Pageable pageable);

    /**
     * 특정 등록 상태인 가진 신고글 목록을 페이징하여 조회합니다.
     * @param status 조회할 등록 상태 (예: PENDING)
     * @param pageable 페이징 정보
     * @return 페이징된 신고글 목록
     */
    Page<Report> findAllByRegistrationStatus(RegistrationStatus status, Pageable pageable);

    // User와 Address를 기준으로 Report가 존재하는지 확인
    boolean existsByUserAndAddress(User user, Address address);

    // 전국 기준 Top 5 주소의 최신 신고글 조회
    @Query(value =
            "SELECT " +
                    "    ANY_VALUE(r.id) AS reportId, " +
                    "    a.addr AS addr, " +
                    "    a.addr_detail AS addrDetail, " +
                    "    ANY_VALUE(r.classification_id) AS classificationId, " +
                    "    ANY_VALUE(r.contract_type_id) AS contractTypeId, " +
                    "    COUNT(r.id) AS count " +
                    "FROM report r " +
                    "JOIN address a ON r.addr_id = a.id " +
                    "WHERE r.registration_status = 'APPROVED' " +
                    "GROUP BY a.id " + // 상세 주소 단위로 그룹화
                    "ORDER BY count DESC, reportId DESC " +
                    "LIMIT 5", // LIMIT으로 간단하게 Top 5 조회
            nativeQuery = true)
    List<ReportWithAddressCount> findTop5ReportsNationwide();

    // 특정 위치 기반 Top 5 주소의 최신 신고글 조회
    @Query(value =
            "SELECT " +
                    "    ANY_VALUE(r.id) AS reportId, " +
                    "    a.addr AS addr, " +
                    "    a.addr_detail AS addrDetail, " +
                    "    ANY_VALUE(r.classification_id) AS classificationId, " +
                    "    ANY_VALUE(r.contract_type_id) AS contractTypeId, " +
                    "    COUNT(r.id) AS count " +
                    "FROM report r " +
                    "JOIN address a ON r.addr_id = a.id " +
                    "WHERE r.registration_status = 'APPROVED' " +
                    "AND ST_Distance_Sphere(POINT(:lng, :lat), POINT(a.lng, a.lat)) <= :radiusMeters " +
                    "GROUP BY a.id " + // 상세 주소 단위로 그룹화
                    "ORDER BY count DESC, reportId DESC " +
                    "LIMIT 5", // LIMIT으로 간단하게 Top 5 조회
            nativeQuery = true)
    List<ReportWithAddressCount> findTop5ReportsByLocation(
            @Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") int radiusMeters);


}
