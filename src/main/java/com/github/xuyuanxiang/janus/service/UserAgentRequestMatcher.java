package com.github.xuyuanxiang.janus.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

public class UserAgentRequestMatcher implements RequestMatcher {
    public static final UserAgentRequestMatcher wechat = new UserAgentRequestMatcher("micromessenger");
    public static final UserAgentRequestMatcher alipay = new UserAgentRequestMatcher("alipayclient");
    private final String ua;

    public UserAgentRequestMatcher(String ua) {
        this.ua = ua;
    }

    @Override
    public boolean matches(HttpServletRequest httpServletRequest) {
        String ua = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);
        return StringUtils.containsIgnoreCase(ua, this.ua);
    }
}
