package com.github.xuyuanxiang.janus.exception;

import com.github.xuyuanxiang.janus.model.WechatBaseResponse;

public class WechatBusinessException extends AuthenticationExceptionWithCode {
    public WechatBusinessException(WechatBaseResponse response) {
        super(ErrorCode.WECHAT_BUSINESS_EXCEPTION, response.getErrcode(), response.getErrmsg());
    }
}
