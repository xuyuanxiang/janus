package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.AuthenticationExceptionWithCode;
import com.github.xuyuanxiang.janus.service.JanusMessageSource;
import com.github.xuyuanxiang.janus.service.UserAgentRequestMatcher;
import com.github.xuyuanxiang.janus.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JanusEntryPoint implements AuthenticationEntryPoint {

    private final JanusProperties properties;
    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        ServletServerHttpRequest httpRequest = new ServletServerHttpRequest(httpServletRequest);
        if (WebUtil.acceptJson(httpRequest)) {
            String code = AuthenticationExceptionWithCode.ErrorCode.UNAUTHORIZED.name();
            String description = JanusMessageSource.INSTANCE.getMessage(code, null, httpServletRequest.getLocale());
            WebUtil.renderJSON(httpServletResponse, HttpStatus.UNAUTHORIZED, code, description);
        } else {
            if (properties.getAlipay().isEnabled() && UserAgentRequestMatcher.alipay.matches(httpServletRequest)) {
                requestCache.saveRequest(httpServletRequest, httpServletResponse);
                WebUtil.sendRedirect(httpServletRequest, httpServletResponse,
                    buildRedirectUri(httpServletRequest, JanusProperties.ALIPAY_AUTH_URL, properties.getAlipay().getAppId()));
            } else if (properties.getWechat().isEnabled() && UserAgentRequestMatcher.wechat.matches(httpServletRequest)) {
                requestCache.saveRequest(httpServletRequest, httpServletResponse);
                WebUtil.sendRedirect(httpServletRequest, httpServletResponse,
                    buildRedirectUri(httpServletRequest, JanusProperties.WECHAT_AUTH_URL, properties.getWechat().getAppid()));
            } else {
                String code = AuthenticationExceptionWithCode.ErrorCode.UNAUTHORIZED.name();
                String message = JanusMessageSource.INSTANCE.getMessage(code, null, httpServletRequest.getLocale());
                WebUtil.sendRedirect(httpServletRequest, httpServletResponse, properties.getFallbackUrl(), code, message);
            }
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
