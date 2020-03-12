package com.github.xuyuanxiang.janus.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @see <a href="https://docs.open.alipay.com/api_2/alipay.user.info.share">alipay.user.info.share</a>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlipayGetUserResponse extends AlipayBaseResponse {
    private AlipayUserInfoShareResponse alipayUserInfoShareResponse;

    @Override
    public boolean isSuccess() {
        return alipayUserInfoShareResponse != null && alipayUserInfoShareResponse.getCode().equals("10000");
    }

    @Data
    public static class AlipayUserInfoShareResponse {
        private String code;
        private String msg;
        private String userId;
        private String avatar;
        private String province;
        private String city;
        private String nickName;
        private String gender;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
            .append("alipayUserInfoShareResponse", alipayUserInfoShareResponse)
            .append("errorResponse", errorResponse)
            .toString();
    }
}
