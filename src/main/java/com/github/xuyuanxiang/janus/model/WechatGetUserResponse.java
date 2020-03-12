package com.github.xuyuanxiang.janus.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 微信接口响应字段
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class WechatGetUserResponse extends WechatBaseResponse {
    private String openid;
    private String nickname;
    private String sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private List<String> privilege;
    private String unionid;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
            .append("openid", openid)
            .append("nickname", nickname)
            .append("sex", sex)
            .append("province", province)
            .append("city", city)
            .append("country", country)
            .append("headimgurl", headimgurl)
            .append("privilege", privilege)
            .append("unionid", unionid)
            .append("errcode", errcode)
            .append("errmsg", errmsg)
            .toString();
    }
}
