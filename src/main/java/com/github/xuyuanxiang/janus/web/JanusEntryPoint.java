package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.AuthenticationExceptionWithCode;
import com.github.xuyuanxiang.janus.service.JanusMessageSource;
import com.github.xuyuanxiang.janus.service.UserAgentRequestMatcher;
import com.github.xuyuanxiang.janus.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JanusEntryPoint implements AuthenticationEntryPoint {

    private final JanusProperties properties;
    private MessageSource messageSource = new JanusMessageSource();

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(httpServletRequest);
        if (WebUtil.acceptJson(httpRequest)) {
            String code = AuthenticationExceptionWithCode.ErrorCode.UNAUTHORIZED.name();
            String message = messageSource.getMessage(code, null, httpServletRequest.getLocale());
            WebUtil.renderJSON(httpServletResponse, code, message);
        } else {
            if (UserAgentRequestMatcher.alipay.matches(httpServletRequest)) {
                saveRequest(httpServletRequest);
                WebUtil.sendRedirect(httpServletRequest, httpServletResponse, buildRedirectUri(httpServletRequest, JanusProperties.ALIPAY_AUTH_URL, properties.getAlipay().getAppId()));
            } else if (UserAgentRequestMatcher.wechat.matches(httpServletRequest)) {
                saveRequest(httpServletRequest);
                WebUtil.sendRedirect(httpServletRequest, httpServletResponse, buildRedirectUri(httpServletRequest, JanusProperties.WECHAT_AUTH_URL, properties.getWechat().getAppid()));
            } else {
                String code = AuthenticationExceptionWithCode.ErrorCode.UNAUTHORIZED.name();
                String message = messageSource.getMessage(code, null, httpServletRequest.getLocale());
                WebUtil.sendRedirect(httpServletRequest, httpServletResponse, properties.getFallbackUrl(), code, message);
            }
        }
    }

    // 保存当前访问URL
    private void saveRequest(HttpServletRequest httpServletRequest) {
        if (StringUtils.isEmpty(properties.getSuccessUrl()) && httpServletRequest.getSession(false) != null) {
            httpServletRequest.getSession().setAttribute(JanusProperties.SAVED_REQUEST,
                UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(httpServletRequest))
                    .build()
                    .toUriString());
        }
    }

    private String buildRedirectUri(HttpServletRequest httpServletRequest, String urlTemplate, String appId) {
        return String.format(urlTemplate, appId, WebUtil.encodeUriComponent(ServletUriComponentsBuilder
            .fromRequest(httpServletRequest)
            .replacePath(JanusProperties.OAUTH_CALLBACK_URL)
            .replaceQuery(null)
            .toUriString()), httpServletRequest.getParameter("state"));
    }
}
