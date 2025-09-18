package somsomcore.zipcheck.global.apiPayload.exception.handler;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

public class S3Handler extends GeneralException {
    public S3Handler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
