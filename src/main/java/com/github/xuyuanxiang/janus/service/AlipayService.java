package com.github.xuyuanxiang.janus.service;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.*;
import com.github.xuyuanxiang.janus.model.AlipayBaseResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetTokenResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
public class AlipayService {
    private final JanusProperties properties;
    private final RestTemplate restTemplate;
    private static final String GATEWAY = "https://openapi.alipay.com/gateway.do";
    private static final String API_GET_TOKEN = "app_id=%s&charset=UTF-8&code=%s&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=%s&timestamp=%s&version=1.0";
    private static final String API_GET_USER = "app_id=%s&auth_token=%s&charset=UTF-8&method=alipay.user.info.share&sign_type=%s&timestamp=%s&version=1.0";

    @Retryable(RetryableExceptionWithCode.class)
    public AlipayGetTokenResponse.AlipaySystemOauthTokenResponse getToken(String authCode, long millis) {
        return request(createApiGetTokenContent(authCode, millis), AlipayGetTokenResponse.class, millis).getAlipaySystemOauthTokenResponse();
    }

    @Retryable(RetryableExceptionWithCode.class)
    public AlipayGetUserResponse.AlipayUserInfoShareResponse getUser(String authToken, long millis) {
        return request(createApiGetUserContent(authToken, millis), AlipayGetUserResponse.class, millis).getAlipayUserInfoShareResponse();
    }

    private <T extends AlipayBaseResponse> T request(String queryParams, Class<T> resClassType, long start) {
        String sign = sign(queryParams);
        String uri = UriComponentsBuilder
            .fromUriString(GATEWAY)
            .query(queryParams)
            .queryParam("sign", sign)
            .build()
            .toString();
        ResponseEntity<T> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(uri, resClassType);
        } catch (Throwable cause) {
            log.error("GET {} failed:", uri, cause);
            throw new AlipayRequestException(cause);
        }
        log.info("GET {} response: {} in {}ms", uri, responseEntity, System.currentTimeMillis() - start);
        HttpStatus status = responseEntity.getStatusCode();
        if (status == HttpStatus.OK) {
            T alipayResponse = responseEntity.getBody();
            if (alipayResponse != null && alipayResponse.isSuccess()) {
                return alipayResponse;
            }
            if (alipayResponse != null && alipayResponse.getErrorResponse() != null) {
                throw new AlipayBusinessException(alipayResponse.getErrorResponse());
            }
            throw new AlipayUnknownError();
        }
        throw new AlipayResponseException(responseEntity);
    }

    private String createApiGetTokenContent(String code, long millis) {
        return String.format(API_GET_TOKEN,
            properties.getAlipay().getAppId(),
            code,
            properties.getAlipay().getSignType().name(),
            DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss"));
    }

    private String createApiGetUserContent(String token, long millis) {
        return String.format(API_GET_USER,
            properties.getAlipay().getAppId(),
            token,
            properties.getAlipay().getSignType().name(),
            DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss"));
    }

    @SneakyThrows
    public String sign(String content) {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] pkcs8EncodedKey = Base64.getDecoder().decode(properties.getAlipay().getPrivateKey());
        PrivateKey priKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKey));
        Signature signature = Signature.getInstance(properties.getAlipay().getSignType() == JanusProperties.SignType.RSA ? "SHA1WithRSA" : "SHA256WithRSA");
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return new String(Base64.getEncoder().encode(signed));
    }
}
