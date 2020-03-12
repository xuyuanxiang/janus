package com.github.xuyuanxiang.janus.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * 支付宝的响应报文：text/html;Charset=UTF-8
 * 微信接口响应报文：text/plain
 */
public class JanusJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    public JanusJackson2HttpMessageConverter() {
        super(Jackson2ObjectMapperBuilder.json()
            .propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build(), MediaType.TEXT_HTML, MediaType.TEXT_PLAIN);
    }
}
