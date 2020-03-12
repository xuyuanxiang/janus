package com.github.xuyuanxiang.janus.exception;

public class WechatUnknownError extends AuthenticationExceptionWithCode {
    public WechatUnknownError() {
        super(ErrorCode.WECHAT_UNKNOWN_ERROR);
    }
}
