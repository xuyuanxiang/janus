package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.model.AlipayGetTokenResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetUserResponse;
import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import com.github.xuyuanxiang.janus.service.AlipayService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

public class AlipayOAuthCallbackFilter extends AbstractOAuthCallbackFilter {
    private final AlipayService alipayService;

    public AlipayOAuthCallbackFilter(JanusProperties properties, RememberMeServices rememberMeServices, AlipayService alipayService) {
        super(properties, rememberMeServices);
        this.alipayService = alipayService;
    }

    @Override
    Authentication handleCallback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String authCode = request.getParameter("auth_code");
        if (StringUtils.isNotEmpty(authCode)) {
            AlipayGetTokenResponse.AlipaySystemOauthTokenResponse tokenResponse = alipayService.getToken(authCode, System.currentTimeMillis());
            if (tokenResponse != null && StringUtils.isNotEmpty(tokenResponse.getAccessToken())) {
                AlipayGetUserResponse.AlipayUserInfoShareResponse userResponse = alipayService.getUser(tokenResponse.getAccessToken(), System.currentTimeMillis());
                return new JanusAuthentication(User.from(userResponse),
                    tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), JanusAuthentication.Credentials.ALIPAY,
                    Duration.ofSeconds(NumberUtils.createInteger(tokenResponse.getExpiresIn())));
            }
        }
        return null;
    }
}
