package com.github.xuyuanxiang.janus.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class WechatRequestException extends RetryableExceptionWithCode {
    public WechatRequestException(Throwable cause) {
        super(ErrorCode.WECHAT_REQUEST_FAILED, ExceptionUtils.getRootCause(cause));
    }
}
