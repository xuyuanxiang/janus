package com.github.xuyuanxiang.janus.exception;

public class AlipayUnknownError extends AuthenticationExceptionWithCode {
    public AlipayUnknownError() {
        super(ErrorCode.ALIPAY_UNKNOWN_ERROR);
    }
}
