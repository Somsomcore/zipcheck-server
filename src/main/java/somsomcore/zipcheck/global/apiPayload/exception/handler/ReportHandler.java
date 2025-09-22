package somsomcore.zipcheck.global.apiPayload.exception.handler;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

public class ReportHandler extends GeneralException {
    public ReportHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
