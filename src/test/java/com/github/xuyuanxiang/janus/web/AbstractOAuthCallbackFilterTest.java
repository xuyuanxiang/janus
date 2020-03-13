package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.AlipayRequestException;
import com.github.xuyuanxiang.janus.model.AlipayGetTokenResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetUserResponse;
import com.github.xuyuanxiang.janus.service.AlipayService;
import com.github.xuyuanxiang.janus.util.WebUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.WebApplicationContext;

import java.net.SocketTimeoutException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(value = {
    "spring.redis.port:2001",
    "janus.failure-url:/error",
})
class AbstractOAuthCallbackFilterTest {

    @Autowired
    WebApplicationContext context;
    MockMvc mvc;
    @MockBean
    AlipayService alipayService;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void handleAuthenticationFailure() throws Exception {
        Throwable cause = new RestClientException("Socket connection timeout", new SocketTimeoutException());
        AlipayRequestException exception = new AlipayRequestException(cause);
        given(alipayService.getToken(eq("123"), anyLong()))
            .willThrow(exception);
        mvc.perform(get("/oauth/callback?auth_code=123").header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/error?error=ALIPAY_REQUEST_FAILED&error_description="
                + WebUtil.encodeUriComponent("支付宝请求失败，请检查网络连接情况。异常：" + ExceptionUtils.getRootCause(cause))));
    }

}
