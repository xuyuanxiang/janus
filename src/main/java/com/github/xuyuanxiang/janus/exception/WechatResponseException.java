package com.github.xuyuanxiang.janus.exception;

import org.springframework.http.ResponseEntity;

public class WechatResponseException extends RetryableExceptionWithCode {
    public WechatResponseException(ResponseEntity responseBody) {
        super(ErrorCode.WECHAT_RESPONSE_ERROR, responseBody);
    }
}
