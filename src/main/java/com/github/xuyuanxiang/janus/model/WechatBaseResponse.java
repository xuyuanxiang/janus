package com.github.xuyuanxiang.janus.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class WechatBaseResponse implements Serializable {
    protected int errcode;
    protected String errmsg;

    public boolean isSuccess() {
        return this.errcode == 0;
    }
}
