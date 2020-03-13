package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import com.github.xuyuanxiang.janus.model.WechatGetTokenResponse;
import com.github.xuyuanxiang.janus.model.WechatGetUserResponse;
import com.github.xuyuanxiang.janus.service.WechatService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

public class WechatOAuthCallbackFilter extends AbstractOAuthCallbackFilter {
    private final WechatService wechatService;

    public WechatOAuthCallbackFilter(JanusProperties properties, WechatService wechatService) {
        super(properties);
        this.wechatService = wechatService;
    }

    @Override
    Authentication handleCallback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        if (StringUtils.isNotEmpty(code)) {
            WechatGetTokenResponse tokenResponse = wechatService.getToken(code);
            if (tokenResponse != null && StringUtils.isNotEmpty(tokenResponse.getOpenid())
                && StringUtils.isNotEmpty(tokenResponse.getAccessToken())) {
                WechatGetUserResponse userResponse = wechatService.getUser(tokenResponse.getAccessToken(), tokenResponse.getOpenid());
                return new JanusAuthentication(User.from(userResponse), tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(), JanusAuthentication.Credentials.WECHAT,
                    Duration.ofSeconds(tokenResponse.getExpiresIn()), tokenResponse.getScope());
            }
        }
        return null;
    }
}
