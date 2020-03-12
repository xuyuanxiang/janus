package com.github.xuyuanxiang.janus.web;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(value = {
    "spring.redis.port:4001",
    "janus.alipay.app-id:2018060660309824",
    "janus.wechat.appid:wx520c15f417810387",
    "janus.fallback-url:/401",
})
@AutoConfigureMockMvc
class JanusEntryPointTest {

    @Autowired
    MockMvc mvc;

    @Test
    void alipay() throws Exception {
        mvc.perform(get("/?state=123").header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2018060660309824&response_type=code&scope=auth_base&redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Fcallback&state=123"));

        mvc.perform(get("/?state=123").header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"code\":\"UNAUTHORIZED\",\"message\":\"请在支付宝或者微信中访问当前页面\"}"));
    }

    @Test
    void wechat() throws Exception {
        mvc.perform(get("/?state=456").header("User-Agent", WechatOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx520c15f417810387&redirect_uri=http%3A%2F%2Flocalhost%2Foauth%2Fcallback&response_type=code&scope=snsapi_base&state=456#wechat_redirect"));

        mvc.perform(get("/?state=123").header("User-Agent", WechatOAuthCallbackFilterTest.MOCK_UA).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"code\":\"UNAUTHORIZED\",\"message\":\"请在支付宝或者微信中访问当前页面\"}"));
    }

    @Test
    void unsupported() throws Exception {
        mvc.perform(get("/?state=456"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/401?code=UNAUTHORIZED&message=" + URLEncoder.encode("请在支付宝或者微信中访问当前页面", "UTF-8")));

        mvc.perform(get("/?state=456").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"code\":\"UNAUTHORIZED\",\"message\":\"请在支付宝或者微信中访问当前页面\"}"));
    }

}
