package somsomcore.zipcheck.global.apiPayload.exception.handler;

import somsomcore.zipcheck.global.apiPayload.code.BaseErrorCode;
import somsomcore.zipcheck.global.apiPayload.exception.GeneralException;

public class ContractTypeHandler extends GeneralException {
    public ContractTypeHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
