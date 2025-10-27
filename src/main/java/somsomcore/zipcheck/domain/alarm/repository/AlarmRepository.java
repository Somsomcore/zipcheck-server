package somsomcore.zipcheck.domain.alarm.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.alarm.entity.Alarm;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Page<Alarm> findByReceiverId(Long receiverId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Alarm a SET a.isConfirmed = true "
         + "WHERE a.receiverId = :receiverId "
         + "AND a.isConfirmed = false "
         + "AND a.createdAt <= :threshold")
    int markAllConfirmedUntil(@Param("receiverId") Long receiverId,
                              @Param("threshold") LocalDateTime threshold);
}
