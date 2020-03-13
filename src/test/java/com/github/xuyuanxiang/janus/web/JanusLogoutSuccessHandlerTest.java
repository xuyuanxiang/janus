package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(value = {
    "janus.logout-request-url:/logout",
    "janus.logout-success-url:/logout/result"
})
class JanusLogoutSuccessHandlerTest {
    @Autowired
    WebApplicationContext context;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void logoutRequestAcceptAll() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new JanusAuthentication(User.builder()
            .id("2088102104794936")
            .avatar("http://tfsimg.alipay.com/images/partner/T1uIxXXbpXXXXXXXX")
            .province("安徽省")
            .city("安庆")
            .nickName("支付宝小二")
            .source(User.Source.ALIPAY)
            .gender(User.Gender.FEMALE)
            .build(), "20120823ac6ffaa4d2d84e7384bf983531473993",
            "30120823ac6ffdsdf2d84e7384bf983531473994", JanusAuthentication.Credentials.ALIPAY));
        mvc.perform(get("/logout").header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logout/result"));
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logoutRequestAcceptRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new JanusAuthentication(User.builder()
            .id("2088102104794936")
            .avatar("http://tfsimg.alipay.com/images/partner/T1uIxXXbpXXXXXXXX")
            .province("安徽省")
            .city("安庆")
            .nickName("支付宝小二")
            .source(User.Source.ALIPAY)
            .gender(User.Gender.FEMALE)
            .build(), "20120823ac6ffaa4d2d84e7384bf983531473993",
            "30120823ac6ffdsdf2d84e7384bf983531473994", JanusAuthentication.Credentials.ALIPAY));
        mvc.perform(post("/logout").header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("{}"));
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
