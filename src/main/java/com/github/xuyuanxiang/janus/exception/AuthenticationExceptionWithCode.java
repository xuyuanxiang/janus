package com.github.xuyuanxiang.janus.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationExceptionWithCode extends AuthenticationException {

    public enum ErrorCode {
        INTERNAL_SERVER_ERROR,
        FORBIDDEN,
        UNAUTHORIZED,
        WECHAT_REQUEST_FAILED,
        WECHAT_RESPONSE_ERROR,
        WECHAT_BUSINESS_EXCEPTION,
        WECHAT_UNKNOWN_ERROR,
        ALIPAY_REQUEST_FAILED,
        ALIPAY_RESPONSE_ERROR,
        ALIPAY_BUSINESS_EXCEPTION,
        ALIPAY_UNKNOWN_ERROR
    }

    @Getter
    private final ErrorCode code;
    @Getter
    private final Object[] args;

    public AuthenticationExceptionWithCode(ErrorCode code, Object... args) {
        super(code.name());
        this.code = code;
        this.args = args;
    }

    public AuthenticationExceptionWithCode(Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER_ERROR.name());
        this.code = ErrorCode.INTERNAL_SERVER_ERROR;
        this.args = new Object[]{cause};
    }
}
