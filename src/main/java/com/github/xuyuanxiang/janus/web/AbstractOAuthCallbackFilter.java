package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.AlipayBusinessException;
import com.github.xuyuanxiang.janus.exception.AuthenticationExceptionWithCode;
import com.github.xuyuanxiang.janus.exception.WechatBusinessException;
import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.service.JanusMessageSource;
import com.github.xuyuanxiang.janus.service.UserAgentRequestMatcher;
import com.github.xuyuanxiang.janus.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOAuthCallbackFilter extends GenericFilter {

    private final RequestMatcher callbackMatcher = new AntPathRequestMatcher(JanusProperties.OAUTH_CALLBACK_URL, "GET");
    private RequestCache requestCache = new HttpSessionRequestCache();
    protected final UserAgentRequestMatcher userAgentRequestMatcher;
    protected final JanusProperties properties;

    @Setter
    protected AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    @Setter
    protected AuthenticationFailureHandler failureHandler = new JanusAuthenticationFailureHandler();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            if (callbackMatcher.matches(request) && userAgentRequestMatcher.matches(request)) {
                log.info("Handle callback: {}", request.getRequestURI());
                Authentication authentication = handleCallback(request, response);
                if (authentication != null) {
                    handleSuccess(request, response, authentication);
                } else {
                    requestCache.removeRequest(request, response);
                    String code = AuthenticationExceptionWithCode.ErrorCode.FORBIDDEN.name();
                    String message = JanusMessageSource.INSTANCE.getMessage(code, null, request.getLocale());
                    WebUtil.sendRedirect(request, response, properties.getDeniedUrl(), code, message);
                }
            }
        } catch (AuthenticationException ex) {
            requestCache.removeRequest(request, response);
            failureHandler.onAuthenticationFailure(request, response, ex);
        } catch (Throwable throwable) {
            requestCache.removeRequest(request, response);
            failureHandler.onAuthenticationFailure(request, response, new AuthenticationExceptionWithCode(throwable));
        } finally {
            if (!response.isCommitted()) {
                chain.doFilter(request, response);
            }
        }
    }

    protected void handleSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof JanusAuthentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authorize succeed: {}", authentication);
            successHandler.onAuthenticationSuccess(request, response, authentication);
        }
    }

    abstract Authentication handleCallback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    class JanusAuthenticationFailureHandler implements AuthenticationFailureHandler {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
            String code;
            String url = properties.getFailureUrl();
            String description;
            if (exception instanceof AuthenticationExceptionWithCode) {
                AuthenticationExceptionWithCode ex = (AuthenticationExceptionWithCode) exception;
                code = ex.getCode().name();
                description = JanusMessageSource.INSTANCE.getMessage(ex.getCode().name(), ex.getArgs(), request.getLocale());
                if (exception instanceof AlipayBusinessException || exception instanceof WechatBusinessException) {
                    url = properties.getDeniedUrl();
                }
            } else {
                code = AuthenticationExceptionWithCode.ErrorCode.FORBIDDEN.name();
                description = JanusMessageSource.INSTANCE.getMessage(code, null, request.getLocale());
                url = properties.getDeniedUrl();
            }
            log.error("Authentication failed: ", exception);
            WebUtil.sendRedirect(request, response, url, code, description);
        }
    }
}
