package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.exception.AlipayRequestException;
import com.github.xuyuanxiang.janus.exception.WechatBusinessException;
import com.github.xuyuanxiang.janus.model.WechatBaseResponse;
import com.github.xuyuanxiang.janus.service.AlipayService;
import com.github.xuyuanxiang.janus.service.WechatService;
import com.github.xuyuanxiang.janus.util.WebUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.WebApplicationContext;

import java.net.SocketTimeoutException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(value = {
    "spring.redis.port:2001",
})
class AbstractOAuthCallbackFilterTest {

    @Autowired
    WebApplicationContext context;
    MockMvc mvc;
    @MockBean
    AlipayService alipayService;
    @MockBean
    WechatService wechatService;
    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void handleError() throws Exception {
        Throwable cause = new RestClientException("Socket connection timeout", new SocketTimeoutException());
        AlipayRequestException exception = new AlipayRequestException(cause);
        given(alipayService.getToken(eq("123"), anyLong()))
            .willThrow(exception);
        mvc.perform(get("/oauth/callback?auth_code=123").header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/500?error=ALIPAY_REQUEST_FAILED&error_description="
                + WebUtil.encodeUriComponent("支付宝请求失败，请检查网络连接情况。异常：" + ExceptionUtils.getRootCause(cause))));
    }

    @Test
    void handleBusinessException() throws Exception {
        WechatBaseResponse baseResponse = new WechatBaseResponse();
        baseResponse.setErrcode(40000);
        baseResponse.setErrmsg("invalid openid");
        WechatBusinessException cause = new WechatBusinessException(baseResponse);
        given(wechatService.getToken("123"))
            .willThrow(cause);
        mvc.perform(get("/oauth/callback?code=123").header("User-Agent", WechatOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/403?error=WECHAT_BUSINESS_EXCEPTION&error_description="
                + WebUtil.encodeUriComponent("微信授权失败（errcode: 40000, errmsg: invalid openid）")));
    }

    @Test
    void handleSystemError() throws Exception {
        Throwable throwable = new RuntimeException();
        given(wechatService.getToken("123"))
            .willThrow(throwable);
        mvc.perform(get("/oauth/callback?code=123").header("User-Agent", WechatOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/500?error=INTERNAL_SERVER_ERROR&error_description="
                + WebUtil.encodeUriComponent("系统错误：" + throwable.toString())));
    }

    @Test
    void handleAuthenticationException() throws Exception {
        Throwable throwable = new BadCredentialsException("balabala");
        given(wechatService.getToken("123"))
            .willThrow(throwable);
        mvc.perform(get("/oauth/callback?code=123").header("User-Agent", WechatOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/403?error=FORBIDDEN&error_description="
                + WebUtil.encodeUriComponent("您没有权限访问当前页面")));
    }
}
