package com.github.xuyuanxiang.janus.service;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class JanusRememberMeService implements RememberMeServices {
    private final static String COOKIE_NAME = "janus-remember-me";
    private final JanusProperties properties;

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        if (properties.isEnableRememberMe()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(COOKIE_NAME)) {
                        if (cookie.getSecure()) {
                            return SerializationUtils.deserialize(cookie.getValue().getBytes());
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        if (properties.isEnableRememberMe() && successfulAuthentication instanceof JanusAuthentication) {
            Cookie cookie = new Cookie(COOKIE_NAME, new String(SerializationUtils.serialize(successfulAuthentication)));
            cookie.setMaxAge((int) properties.getRememberMeTTL().getSeconds());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            response.addCookie(cookie);
        }
    }
}
