package com.github.xuyuanxiang.janus.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class AlipayRequestException extends RetryableExceptionWithCode {
    public AlipayRequestException(Throwable cause) {
        super(ErrorCode.ALIPAY_REQUEST_FAILED, ExceptionUtils.getRootCause(cause));
    }
}
