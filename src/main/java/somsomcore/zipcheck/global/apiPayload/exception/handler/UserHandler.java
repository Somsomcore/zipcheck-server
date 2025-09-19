package somsomcore.zipcheck.global.apiPayload.exception.handler;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
