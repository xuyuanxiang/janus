package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import com.github.xuyuanxiang.janus.util.WebUtil;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(value = {
    "spring.redis.port:5001",
    "janus.logout-request-url:/logout",
    "janus.logout-success-url:/logout/result"
})
class JanusAccessDeniedHandlerTest {
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
    void handleAccessDenied() throws Exception {
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
        mvc.perform(get("/api/protected")
            .header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/403?error=FORBIDDEN&error_description=" + WebUtil.encodeUriComponent("您没有权限访问当前页面")));
        mvc.perform(get("/api/protected")
            .header("User-Agent", AlipayOAuthCallbackFilterTest.MOCK_UA)
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden())
            .andExpect(content().json("{\"error\":\"FORBIDDEN\",\"error_description\":\"您没有权限访问当前页面\"}"));
        SecurityContextHolder.clearContext();
    }
}
