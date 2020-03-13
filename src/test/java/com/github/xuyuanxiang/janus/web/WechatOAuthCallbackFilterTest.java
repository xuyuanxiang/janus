package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.model.*;
import com.github.xuyuanxiang.janus.service.WechatService;
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
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(value = {
    "spring.redis.port:6001",
    "janus.wechat.app-id:123",
    "janus.logout-request-url:/logout",
    "janus.logout-success-url:/logout/result"
})
class WechatOAuthCallbackFilterTest {
    public final static String MOCK_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13F69 MicroMessenger/7.0.8(0x17000820) NetType/4G Language/zh_CN miniProgram";
    @Autowired
    WebApplicationContext context;
    MockMvc mvc;

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
    void oAuthCallback() throws Exception {
        WechatGetTokenResponse response = new WechatGetTokenResponse();
        response.setAccessToken("ACCESS_TOKEN");
        response.setExpiresIn(7200);
        response.setOpenid("OPENID");
        response.setRefreshToken("REFRESH_TOKEN");
        response.setScope("SCOPE");
        given(wechatService.getToken("CODE")).willReturn(response);
        WechatGetUserResponse userResponse = new WechatGetUserResponse();
        userResponse.setCity("CITY");
        userResponse.setProvince("PROVINCE");
        userResponse.setCountry("COUNTRY");
        userResponse.setOpenid("OPENID");
        userResponse.setHeadimgurl("http://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46");
        userResponse.setNickname("NICKNAME");
        userResponse.setSex("1");
        userResponse.setUnionid("o6_bmasdasdsad6_2sgVt7hMZOPfL");
        userResponse.setPrivilege(Arrays.asList("PRIVILEGE1", "PRIVILEGE2"));
        userResponse.setCountry("COUNTRY");
        given(wechatService.getUser("ACCESS_TOKEN", "OPENID")).willReturn(userResponse);

        mvc.perform(get("/oauth/callback?code=CODE").header("User-Agent", MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withAuthentication(new JanusAuthentication(User.builder()
                .id("OPENID")
                .avatar("http://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46")
                .province("PROVINCE")
                .city("CITY")
                .nickName("NICKNAME")
                .source(User.Source.WECHAT)
                .gender(User.Gender.MALE)
                .wechatCountry("COUNTRY")
                .wechatUnionId("o6_bmasdasdsad6_2sgVt7hMZOPfL")
                .wechatPrivilege(Arrays.asList("PRIVILEGE1", "PRIVILEGE2"))
                .build(), "ACCESS_TOKEN",
                "REFRESH_TOKEN", JanusAuthentication.Credentials.WECHAT, Duration.ofSeconds(7200), "SCOPE")))
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void logout() throws Exception {
        mvc.perform(get("/logout").header("User-Agent", MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logout/result"));
    }

}
