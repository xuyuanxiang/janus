package com.github.xuyuanxiang.janus.model;

import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.time.Duration;
import java.util.Date;

public class JanusAuthentication extends AbstractAuthenticationToken {
    @Getter
    private final String accessToken;
    @Getter
    private final String refreshToken;
    @Getter
    private final User user;
    @Getter
    private final Date ctime = new Date();
    @Getter
    private final Duration expiresIn;
    private final Credentials credentials;

    public JanusAuthentication(User user, String accessToken, String refreshToken, Credentials credentials) {
        this(user, accessToken, refreshToken, credentials, Duration.ofSeconds(3600));
    }

    public JanusAuthentication(User user, String accessToken, String refreshToken, Credentials credentials, Duration expiresIn) {
        super(null);
        this.user = user;
        this.accessToken = accessToken;
        this.credentials = credentials;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials.name();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    public enum Credentials {
        /**
         * 通过支付宝网页授权登录
         */
        ALIPAY,
        /**
         * 通过微信公众号网页授权登录
         */
        WECHAT,
        /**
         * 通过本地Remember Me 安全Cookie登录
         */
        REMEMBER_ME,
        /**
         * 微信第三方平台代公众号发起网页授权登录
         */
        WECHAT_PLATFORM
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
            .append("accessToken", accessToken)
            .append("refreshToken", refreshToken)
            .append("user", user)
            .append("ctime", ctime)
            .append("expiresIn", expiresIn)
            .append("credentials", credentials)
            .toString();
    }
}
