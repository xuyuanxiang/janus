package com.github.xuyuanxiang.janus.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public static void renderJSON(final HttpServletResponse response, HttpStatus status, String error, String errorDescription) {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        out.print("{\"error\":\"" + error + "\",\"error_description\":\"" + errorDescription + "\"}");
        out.flush();
    }

    @SneakyThrows
    public static void renderJSON(final HttpServletResponse response) {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        final PrintWriter out = response.getWriter();
        out.print("{}");
        out.flush();
    }

    @SneakyThrows
    public static void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, String location) {
        log.info("Redirect: {}", location);
        redirectStrategy.sendRedirect(request, response, location);
    }

    @SneakyThrows
    public static void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, String location, String error, String errorDescription) {
        log.info("Redirect: {}", location);
        redirectStrategy.sendRedirect(request, response, UriComponentsBuilder
            .fromPath(location)
            .queryParam("error", error)
            .queryParam("error_description", encodeUriComponent(errorDescription))
            .build().toUriString());
    }

    @SneakyThrows
    public static String encodeUriComponent(String input) {
        return URLEncoder.encode(input, "UTF-8");
    }
}
