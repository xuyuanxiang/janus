package com.github.xuyuanxiang.janus.exception;

public class WechatRequestException extends RetryableExceptionWithCode {
    public WechatRequestException(Throwable cause) {
        super(ErrorCode.WECHAT_REQUEST_FAILED, cause);
    }
}
