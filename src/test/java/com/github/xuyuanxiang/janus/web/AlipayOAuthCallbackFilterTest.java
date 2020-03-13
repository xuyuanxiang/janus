package com.github.xuyuanxiang.janus.web;

import com.github.xuyuanxiang.janus.model.AlipayGetTokenResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetUserResponse;
import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import com.github.xuyuanxiang.janus.service.AlipayService;
import com.github.xuyuanxiang.janus.util.WebUtil;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
class AlipayOAuthCallbackFilterTest {
    public static final String MOCK_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_1_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/17A878 ChannelId(3) NebulaSDK/1.8.100112 Nebula WK PSDType(1) AlipayDefined(nt:WIFI,ws:414|832|2.0) AliApp(AP/10.1.80.6060) AlipayClient/10.1.80.6060 Alipay Language/zh-Hans Region/CN";
    @MockBean
    AlipayService alipayService;
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
    void oAuthCallback() throws Exception {
        AlipayGetTokenResponse.AlipaySystemOauthTokenResponse tokenResponse = new AlipayGetTokenResponse.AlipaySystemOauthTokenResponse();
        tokenResponse.setAccessToken("20120823ac6ffaa4d2d84e7384bf983531473993");
        tokenResponse.setExpiresIn("3600");
        tokenResponse.setRefreshToken("30120823ac6ffdsdf2d84e7384bf983531473994");
        tokenResponse.setUserId("2088102150477652");
        given(alipayService.getToken(eq("666"), anyLong())).willReturn(tokenResponse);
        AlipayGetUserResponse.AlipayUserInfoShareResponse userResponse = new AlipayGetUserResponse.AlipayUserInfoShareResponse();
        userResponse.setUserId("2088102104794936");
        userResponse.setAvatar("http://tfsimg.alipay.com/images/partner/T1uIxXXbpXXXXXXXX");
        userResponse.setProvince("安徽省");
        userResponse.setCity("安庆");
        userResponse.setNickName("支付宝小二");
        userResponse.setGender("F");
        given(alipayService.getUser(eq("20120823ac6ffaa4d2d84e7384bf983531473993"), anyLong())).willReturn(userResponse);

        mvc.perform(get("/oauth/callback?auth_code=666").header("User-Agent", MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withAuthentication(new JanusAuthentication(User.builder()
                .id("2088102104794936")
                .avatar("http://tfsimg.alipay.com/images/partner/T1uIxXXbpXXXXXXXX")
                .province("安徽省")
                .city("安庆")
                .nickName("支付宝小二")
                .source(User.Source.ALIPAY)
                .gender(User.Gender.FEMALE)
                .build(), "20120823ac6ffaa4d2d84e7384bf983531473993",
                "30120823ac6ffdsdf2d84e7384bf983531473994", JanusAuthentication.Credentials.ALIPAY)))
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void rejectionCallback() throws Exception {
        mvc.perform(get("/oauth/callback").header("User-Agent", MOCK_UA))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/403?error=FORBIDDEN&error_description=" + WebUtil.encodeUriComponent("您没有权限访问当前页面")));
    }
}
