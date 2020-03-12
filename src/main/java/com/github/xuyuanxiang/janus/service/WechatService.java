package com.github.xuyuanxiang.janus.service;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.*;
import com.github.xuyuanxiang.janus.model.WechatBaseResponse;
import com.github.xuyuanxiang.janus.model.WechatGetTokenResponse;
import com.github.xuyuanxiang.janus.model.WechatGetUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class WechatService {
    private static final String API_GET_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String API_GET_USER = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";
    private final JanusProperties properties;
    private final RestTemplate restTemplate;

    @Retryable(RetryableExceptionWithCode.class)
    public WechatGetTokenResponse getToken(String code) {
        return request(createGetTokenApi(code), WechatGetTokenResponse.class);
    }

    @Retryable(RetryableExceptionWithCode.class)
    public WechatGetUserResponse getUser(String token, String openid) {
        return request(createGetUserApi(token, openid), WechatGetUserResponse.class);
    }

    private <T extends WechatBaseResponse> T request(String uri, Class<T> resClassType) {
        long start = System.currentTimeMillis();
        ResponseEntity<T> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(uri, resClassType);
        } catch (Throwable cause) {
            log.error("GET {} failed:", uri, cause);
            throw new WechatRequestException(cause);
        }
        log.info("GET {} {} - {}ms", uri, responseEntity, System.currentTimeMillis() - start);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            T response = responseEntity.getBody();
            if (response != null) {
                if (response.isSuccess()) {
                    return response;
                } else {
                    throw new WechatBusinessException(response);
                }
            }
            throw new WechatUnknownError();
        }
        throw new WechatResponseException(responseEntity);
    }

    private String createGetTokenApi(String code) {
        return String.format(API_GET_TOKEN,
            properties.getWechat().getAppid(), properties.getWechat().getSecret(), code);
    }

    private String createGetUserApi(String token, String openid) {
        return String.format(API_GET_USER, token, openid);
    }
}
