package somsomcore.zipcheck.domain.alarm.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    REPORT_PENDING_REVIEW("새롭게 검토할 신고글이 등록되었습니다. 확인해주세요."),
    REPORT_SUBMITTED("회원님의 신고글이 정상적으로 등록되었습니다. 검토 후 처리 결과를 알려드리겠습니다."),
    REPORT_REJECTED("신고글이 반려되었습니다.");

    private final String defaultMessage;
}
