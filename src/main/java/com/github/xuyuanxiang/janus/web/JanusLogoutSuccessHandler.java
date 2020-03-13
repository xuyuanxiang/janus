package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JanusLogoutSuccessHandler implements LogoutSuccessHandler {
    private final JanusProperties properties;
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (WebUtil.acceptJson(new ServletServerHttpRequest(request))) {
            WebUtil.renderJSON(response);
        } else {
            WebUtil.sendRedirect(request, response, properties.getLogoutSuccessUrl());
        }
    }
}
