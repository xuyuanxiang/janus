package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.AuthenticationExceptionWithCode;
import com.github.xuyuanxiang.janus.service.JanusMessageSource;
import com.github.xuyuanxiang.janus.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JanusAccessDeniedHandler implements AccessDeniedHandler {
    private final JanusProperties properties;
    private MessageSource messageSource = new JanusMessageSource();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String code = AuthenticationExceptionWithCode.ErrorCode.FORBIDDEN.name();
        String message = messageSource.getMessage(code, null, request.getLocale());
        if (WebUtil.acceptJson(new ServletServerHttpRequest(request))) {
            WebUtil.renderJSON(response, code, message);
        } else {
            WebUtil.sendRedirect(request, response, properties.getDeniedUrl(), code, message);
        }
    }

}
