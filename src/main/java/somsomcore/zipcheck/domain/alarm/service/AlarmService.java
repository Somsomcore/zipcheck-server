package somsomcore.zipcheck.domain.alarm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import somsomcore.zipcheck.domain.alarm.repository.AlarmEmitterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import somsomcore.zipcheck.domain.alarm.dto.AlarmResponseDTO;
import somsomcore.zipcheck.domain.alarm.entity.Alarm;
import somsomcore.zipcheck.domain.alarm.entity.enums.AlarmType;
import somsomcore.zipcheck.domain.alarm.repository.AlarmRepository;
import somsomcore.zipcheck.domain.report.entity.Report;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.Role;
import somsomcore.zipcheck.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AlarmService {

    private static final String TITLE_PENDING = "신고 검토 요청";
    private static final String TITLE_SUBMITTED = "신고 접수 안내";
    private static final String TITLE_REJECTED = "신고 반려 안내";

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final AlarmEmitterRepository alarmEmitterRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

    @Transactional(readOnly = true)
    public AlarmResponseDTO.AlarmListResultDTO getAlarms(Long receiverId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Alarm> alarmPage = alarmRepository.findByReceiverId(receiverId, pageable);

        List<AlarmResponseDTO.AlarmItemDTO> items = alarmPage.getContent().stream()
                .map(this::toAlarmItemDTO)
                .collect(Collectors.toList());

        return AlarmResponseDTO.AlarmListResultDTO.builder()
                .alarms(items)
                .totalPages(alarmPage.getTotalPages())
                .currentPage(alarmPage.getNumber())
                .totalElements(alarmPage.getTotalElements())
                .isLast(alarmPage.isLast())
                .build();
    }

    public AlarmResponseDTO.ConfirmResultDTO confirmAllUntilNow(Long receiverId) {
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = alarmRepository.markAllConfirmedUntil(receiverId, now);

        return AlarmResponseDTO.ConfirmResultDTO.builder()
                .confirmedCount((long) updatedCount)
                .confirmedAt(now)
                .build();
    }

    public void notifyReportSubmitted(Report report) {
        Long senderId = report.getUser().getId();

        createAlarm(
                AlarmType.REPORT_SUBMITTED,
                TITLE_SUBMITTED,
                report.getUser().getId(),
                senderId,
                report);

        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        for (User admin : admins) {
            createAlarm(
                    AlarmType.REPORT_PENDING_REVIEW,
                    TITLE_PENDING,
                    admin.getId(),
                    senderId,
                    report);
        }
    }

    public void notifyReportRejected(Report report, Long adminId) {
        createAlarm(
                AlarmType.REPORT_REJECTED,
                TITLE_REJECTED,
                report.getUser().getId(),
                adminId,
                report);
    }

    private void createAlarm(AlarmType type,
            String title,
            Long receiverId,
            Long senderId,
            Report report) {

        Alarm alarm = Alarm.builder()
                .type(type)
                .title(title)
                .content(type.getDefaultMessage())
                .receiverId(receiverId)
                .senderId(senderId)
                .report(report)
                .build();

        alarmRepository.save(alarm);

        // SSE 알람 실시간 전송 (Push)
        String receiverIdStr = String.valueOf(receiverId);
        String eventId = receiverIdStr + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = alarmEmitterRepository.findAllEmitterStartWithByMemberId(receiverIdStr);
        emitters.forEach((key, emitter) -> {
            alarmEmitterRepository.saveEventCache(key, alarm);
            sendNotification(emitter, eventId, key, toAlarmItemDTO(alarm));
        });
    }

    public SseEmitter subscribe(Long userId, String lastEventId) {
        String emitterId = userId + "_" + System.currentTimeMillis();
        SseEmitter emitter = alarmEmitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> alarmEmitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> alarmEmitterRepository.deleteById(emitterId));
        emitter.onError((e) -> alarmEmitterRepository.deleteById(emitterId));

        // 503 방지: 더미 데이터 전송
        String eventId = userId + "_" + System.currentTimeMillis();
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userId + "]");

        // 미수신 이벤트 전송 로직 (옵션)
        if (lastEventId != null && !lastEventId.isEmpty()) {
            Map<String, Object> events = alarmEmitterRepository
                    .findAllEventCacheStartWithByMemberId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
        }

        return emitter;
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("alarm")
                    .data(data));
        } catch (IOException exception) {
            alarmEmitterRepository.deleteById(emitterId);
        }
    }

    private AlarmResponseDTO.AlarmItemDTO toAlarmItemDTO(Alarm alarm) {
        Long reportId = alarm.getReport() != null ? alarm.getReport().getId() : null;
        return AlarmResponseDTO.AlarmItemDTO.builder()
                .alarmId(alarm.getId())
                .notificationType(alarm.getType())
                .notificationContent(alarm.getContent())
                .reportId(reportId)
                .confirmed(alarm.getIsConfirmed())
                .createdAt(alarm.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean hasUnreadAlarms(Long receiverId) {
        return alarmRepository.existsByReceiverIdAndIsConfirmedFalse(receiverId);
    }
}
