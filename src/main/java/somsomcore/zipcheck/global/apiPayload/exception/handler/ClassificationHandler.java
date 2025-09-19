package somsomcore.zipcheck.global.apiPayload.exception.handler;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

public class ClassificationHandler extends GeneralException {
    public ClassificationHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
