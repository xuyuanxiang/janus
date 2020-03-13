package com.github.xuyuanxiang.janus.service;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.*;
import com.github.xuyuanxiang.janus.model.WechatGetTokenResponse;
import com.github.xuyuanxiang.janus.model.WechatGetUserResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

class WechatServiceTest {

    @Mock
    RestTemplate restTemplate;
    JanusProperties properties;
    WechatService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        JanusProperties.WechatConfiguration wechat = new JanusProperties.WechatConfiguration();
        wechat.setAppid("APPID");
        wechat.setSecret("SECRET");
        properties = new JanusProperties();
        properties.setWechat(wechat);
        service = new WechatService(properties, restTemplate);
    }

    @Test
    void realGetToken() {
        WechatService wechatService = new WechatService(properties, new RestTemplateBuilder()
            .setConnectTimeout(properties.getConnectionTimeout())
            .setReadTimeout(properties.getReadTimeout())
            .messageConverters(new JanusJackson2HttpMessageConverter())
            .build());
        try {
            wechatService.getToken("CODE");
            fail("WechatBusinessException should be raised");
        } catch (WechatBusinessException ignored) {

        }
    }

    @Test
    void getToken() {
        WechatGetTokenResponse expectResponse = new WechatGetTokenResponse();
        expectResponse.setAccessToken("ACCESS_TOKEN");
        expectResponse.setExpiresIn(7200);
        expectResponse.setOpenid("OPENID");
        expectResponse.setRefreshToken("REFRESH_TOKEN");
        ResponseEntity<WechatGetTokenResponse> responseEntity = new ResponseEntity<>(expectResponse, HttpStatus.OK);
        given(restTemplate.getForEntity("https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code", WechatGetTokenResponse.class))
            .willReturn(responseEntity);
        WechatGetTokenResponse actualResponse = service.getToken("CODE");
        assertEquals(expectResponse, actualResponse);
    }

    @Test
    void getUser() {
        WechatGetUserResponse expectResponse = new WechatGetUserResponse();
        expectResponse.setCity("CITY");
        expectResponse.setProvince("PROVINCE");
        expectResponse.setCountry("COUNTRY");
        expectResponse.setOpenid("OPENID");
        expectResponse.setHeadimgurl("http://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46");
        expectResponse.setNickname("NICKNAME");
        expectResponse.setSex("1");
        expectResponse.setUnionid("o6_bmasdasdsad6_2sgVt7hMZOPfL");
        expectResponse.setPrivilege(Arrays.asList("PRIVILEGE1", "PRIVILEGE2"));
        expectResponse.setCountry("COUNTRY");
        ResponseEntity<WechatGetUserResponse> responseEntity = new ResponseEntity<>(expectResponse, HttpStatus.OK);
        given(restTemplate.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN", WechatGetUserResponse.class))
            .willReturn(responseEntity);
        WechatGetUserResponse actualResponse = service.getUser("ACCESS_TOKEN", "OPENID");
        assertEquals(expectResponse, actualResponse);
    }

    @Test
    void requestFailed() throws Exception {
        RestClientException cause = new RestClientException("No such hostname", new UnknownHostException());
        given(restTemplate.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN", WechatGetUserResponse.class))
            .willThrow(cause);
        try {
            service.getUser("ACCESS_TOKEN", "OPENID");
            fail("WechatRequestException should be raised");
        } catch (WechatRequestException ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.WECHAT_REQUEST_FAILED);
            assertEquals(ex.getArgs()[0], ExceptionUtils.getRootCause(cause));
        }
    }

    @Test
    void responseError() throws Exception {
        ResponseEntity<WechatGetUserResponse> responseEntity = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        given(restTemplate.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN", WechatGetUserResponse.class))
            .willReturn(responseEntity);
        try {
            service.getUser("ACCESS_TOKEN", "OPENID");
            fail("WechatResponseException should be raised");
        } catch (WechatResponseException ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.WECHAT_RESPONSE_ERROR);
            assertEquals(ex.getArgs()[0], responseEntity);
        }
    }

    @Test
    void responseEmptyBody() throws Exception {
        ResponseEntity<WechatGetUserResponse> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        given(restTemplate.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN", WechatGetUserResponse.class))
            .willReturn(responseEntity);
        try {
            service.getUser("ACCESS_TOKEN", "OPENID");
            fail("WechatUnknownError should be raised");
        } catch (WechatUnknownError ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.WECHAT_UNKNOWN_ERROR);
        }
    }

    @Test
    void businessException() throws Exception {
        WechatGetUserResponse response = new WechatGetUserResponse();
        response.setErrcode(40003);
        response.setErrmsg("invalid openid");
        ResponseEntity<WechatGetUserResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        given(restTemplate.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN", WechatGetUserResponse.class))
            .willReturn(responseEntity);
        try {
            service.getUser("ACCESS_TOKEN", "OPENID");
            fail("WechatBusinessException should be raised");
        } catch (WechatBusinessException ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.WECHAT_BUSINESS_EXCEPTION);
            assertEquals(ex.getArgs()[0], 40003);
            assertEquals(ex.getArgs()[1], "invalid openid");
        }
    }
}
