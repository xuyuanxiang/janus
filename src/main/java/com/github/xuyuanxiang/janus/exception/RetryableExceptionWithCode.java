package com.github.xuyuanxiang.janus.exception;

public class RetryableExceptionWithCode extends AuthenticationExceptionWithCode {
    public RetryableExceptionWithCode(ErrorCode code, Object ...args) {
        super(code, args);
    }
}

