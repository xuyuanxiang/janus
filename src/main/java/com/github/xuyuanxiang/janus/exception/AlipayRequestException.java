package com.github.xuyuanxiang.janus.exception;

public class AlipayRequestException extends RetryableExceptionWithCode {
    public AlipayRequestException(Throwable cause) {
        super(ErrorCode.ALIPAY_REQUEST_FAILED, cause);
    }
}
