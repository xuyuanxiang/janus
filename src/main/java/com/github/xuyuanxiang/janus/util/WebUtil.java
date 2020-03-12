package com.github.xuyuanxiang.janus.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;

@Slf4j
public class WebUtil {
    private static RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public static boolean acceptJson(final ServletServerHttpRequest httpRequest) {
        if (httpRequest.getHeaders() != null &&
            httpRequest.getHeaders().getAccept() != null) {
            return httpRequest.getHeaders().getAccept().stream()
                .anyMatch(it -> !it.isWildcardType() && it.includes(MediaType.APPLICATION_JSON));
        }
        return false;
    }

    @SneakyThrows
    public static void renderJSON(final HttpServletResponse response, String code, String message) {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        out.print("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
        out.flush();
    }

    @SneakyThrows
    public static void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, String location) {
        log.info("Redirect: {}", location);
        redirectStrategy.sendRedirect(request, response, location);
    }

    @SneakyThrows
    public static void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, String location, String code, String message) {
        log.info("Redirect: {}", location);
        redirectStrategy.sendRedirect(request, response, UriComponentsBuilder
            .fromPath(location).queryParam("code", code).queryParam("message", encodeUriComponent(message)).build().toUriString());
    }

    @SneakyThrows
    public static String encodeUriComponent(String input) {
        return URLEncoder.encode(input, "UTF-8");
    }
}
