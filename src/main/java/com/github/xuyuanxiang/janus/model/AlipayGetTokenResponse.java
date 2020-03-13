package com.github.xuyuanxiang.janus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class AlipayGetTokenResponse extends AlipayBaseResponse {
    private AlipaySystemOauthTokenResponse alipaySystemOauthTokenResponse;

    @JsonIgnore
    public boolean isSuccess() {
        return alipaySystemOauthTokenResponse != null
            && StringUtils.isNotEmpty(alipaySystemOauthTokenResponse.getUserId());
    }

    @Data
    public static class AlipaySystemOauthTokenResponse {
        private String userId;
        private String accessToken;
        private String refreshToken;
        private String expiresIn;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("userId", userId)
                .append("accessToken", accessToken)
                .append("refreshToken", refreshToken)
                .append("expiresIn", expiresIn)
                .toString();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
            .append("alipaySystemOauthTokenResponse", alipaySystemOauthTokenResponse)
            .append("errorResponse", errorResponse)
            .toString();
    }
}
