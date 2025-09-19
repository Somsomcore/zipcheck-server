package somsomcore.zipcheck.global.apiPayload.exception.handler;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

public class AddressHandler extends GeneralException {
    public AddressHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
