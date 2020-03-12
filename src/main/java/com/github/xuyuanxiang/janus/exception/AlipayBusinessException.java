package com.github.xuyuanxiang.janus.exception;

import com.github.xuyuanxiang.janus.model.AlipayBaseResponse;

public class AlipayBusinessException extends AuthenticationExceptionWithCode {
    public AlipayBusinessException(AlipayBaseResponse.ErrorResponse response) {
        super(ErrorCode.ALIPAY_BUSINESS_EXCEPTION, response.getCode(), response.getMsg(), response.getSubCode(), response.getSubMsg());
    }
}
