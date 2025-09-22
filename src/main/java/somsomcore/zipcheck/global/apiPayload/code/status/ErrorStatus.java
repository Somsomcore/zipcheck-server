package somsomcore.zipcheck.global.apiPayload.code.status;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이것은 임시 에러 메시지입니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4000", "사용자를 찾을 수 없습니다."),

    // JWT 인증
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4001", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4002", "만료된 토큰입니다."),
    TOKEN_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4003", "토큰에 해당하는 사용자를 찾을 수 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH4004", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4005", "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4006", "만료된 리프레시 토큰입니다."),
    OAUTH_USER_INFO_FAILED(HttpStatus.BAD_REQUEST, "AUTH4007", "소셜 사용자 정보 조회에 실패했습니다."),

    // 주소
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS4000", "입력하신 주소를 찾을 수 없습니다. 도로명/지번과 상세주소를 다시 확인해 주세요."),
    GEOCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ADDRESS4001", "주소 좌표를 조회하는 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."),

    // 계약 형태
    CONTRACTTYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTRACTTYPE4000", "계약 형태를 찾을 수 없습니다."),

    // 사기 분류
    CLASSIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CLASSIFICATION4000", "사기 분류를 찾을 수 없습니다."),

    // 신고글
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4000", "신고글을 찾을 수 없습니다."),

    // 파일(NCP-S3)
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE4000", "업로드할 파일이 비어있습니다."),
    FILE_NOT_PDF(HttpStatus.BAD_REQUEST, "FILE4001", "PDF 형식만 업로드 가능합니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE4002", "파일을 찾을 수 없습니다."),
    FILE_INVALID_PATH(HttpStatus.BAD_REQUEST, "FILE4003", "허용되지 않은 경로입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE5001", "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE5002", "파일 삭제에 실패했습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }

}