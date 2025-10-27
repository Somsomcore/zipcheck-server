package somsomcore.zipcheck.domain.alarm.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import somsomcore.zipcheck.domain.alarm.entity.enums.AlarmType;

public class AlarmResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmListResultDTO {
        private List<AlarmItemDTO> alarms;
        private Integer totalPages;
        private Integer currentPage;
        private Long totalElements;
        private Boolean isLast;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmItemDTO {
        private Long alarmId;
        private AlarmType notificationType;
        private String notificationContent;
        private Long reportId;
        private Boolean confirmed;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmResultDTO {
        private Long confirmedCount;
        private LocalDateTime confirmedAt;
    }
}
