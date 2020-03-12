package com.github.xuyuanxiang.janus.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 微信接口响应字段
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class WechatGetTokenResponse extends WechatBaseResponse {
    private String accessToken;
    private int expiresIn;
    private String refreshToken;
    private String openid;
    private String scope;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
            .append("accessToken", accessToken)
            .append("expiresIn", expiresIn)
            .append("refreshToken", refreshToken)
            .append("openid", openid)
            .append("scope", scope)
            .append("errcode", errcode)
            .append("errmsg", errmsg)
            .toString();
    }
}
