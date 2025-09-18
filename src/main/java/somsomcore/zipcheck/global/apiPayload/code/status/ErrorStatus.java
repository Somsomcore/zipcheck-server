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