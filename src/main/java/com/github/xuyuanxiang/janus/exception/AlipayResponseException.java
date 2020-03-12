package com.github.xuyuanxiang.janus.exception;

import org.springframework.http.ResponseEntity;

public class AlipayResponseException extends RetryableExceptionWithCode {
    public AlipayResponseException(ResponseEntity responseBody) {
        super(ErrorCode.ALIPAY_RESPONSE_ERROR, responseBody);
    }
}
