package com.github.xuyuanxiang.janus.service;

import com.github.xuyuanxiang.janus.JanusProperties;
import com.github.xuyuanxiang.janus.exception.*;
import com.github.xuyuanxiang.janus.model.AlipayBaseResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetTokenResponse;
import com.github.xuyuanxiang.janus.model.AlipayGetUserResponse;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

class AlipayServiceTest {
    AlipayService alipayService;
    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        JanusProperties.AlipayConfiguration alipay = new JanusProperties.AlipayConfiguration();
        alipay.setAppId("2018060660309824");
        alipay.setPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXMOfkEypvCyJ5kEhYthqXZL4g6IWedNbJYMFjFkOzEZpEX+lcD0zSfIaBFBYt+yfpv0vYp8YoQdhyaL2v1MYeXzN1t1zlOfkN2N5WUIo3ILOz0hlU0NNR4ckhTQMaQZlWpUXPRI6+qudDWaDg/gT8kw6kre2vaJq48VFdwqsCRVp6VExjg8z+NDT94gwCmwrZu5yx8n6eGkLmNjAkYVgIvblMEnS6eYlpYvsucN2YLBCQf4AtimkSoqL8TcCN03bDj3kI+rHr7LBVL+XhrvKgxovWmmzD4nulk0qyj8fzbQyQO9azv9IrmSUyOHBCi2oXDG76crjFqVSi/atBC4r5AgMBAAECggEATaLSYN6qmozYLh412D/ilb0omJNuEbkjlhL2GyCSsQAn/FZX6Wr94tQI6X1cCk5+51vQ4bb9XSy6rb01MnYLKgtuzFLVA+xqBH9JZH9FChvjy0HmuhPlf5V2h4AZSMFIsSJ7H3yv5B60VHRQ1Vf7TYtiKJQbikgnszJPutq8n5Qb1O93/EfwQmwEYfYYJlZwxTDKPNNJ6wDUclj0ymkfpTBbbheuzmlAfFcNL8+75eIRHPyxsHN9LAUl0w9leb+WLujSnxOkMMbHy1J2mL1oywNmZn2F4l2GQEiKQC2BKFV+WwtteMUaudac60c336gIG8RbJLTrwzNNoS6Q7yHyAQKBgQDeilAfVGDSW+6Gx3OslndIKR+tSzC1zWL573NdRLr1QULf5W/EAzia3fMsL35MZhieSdorS9YTZYsrYwcDAZZgk1asM613OKrubA+CmE4KjYVvuhzV2woiNvz3E5LK0RVqLD9IcmgFPEj/dvF2q6N0YKvIB75r4Qvhd9W8+y54OQKBgQCt7FMZiKMR7IXBlJCSvH0LKj/JqHxH4ZcedkRuewouqya15yvDMVDejsmRV4B0v4d/4nJ/AiWq1z1pjfaFIA7teL9XingZwDwV+6PeYeF8A41Kj44teBcgKPQxjR0Qr3x0bqsiLi8dQB7Dp5q4RMnp7ErzLYWBfq4aXijlXgoowQKBgQDFo9kcufdzW6ovkRuuf8NYFw3G/iw4ijwI9RxkDRJtlpQp/L6SOroe8FdzOorUlyfuDHDyWtu5RkmfMsebZ+GBy9kB/rNkWrOUI+xyc4l73cxQOd46H9qUIHnxhTStY5u9O6bIVLMFU1ERcTVpy23TgbeOYzI+5ROEHsV4nSvcIQKBgDZNaQy52qBIBuMP7avC6g/IWy4tStBuoO5D4s3T4LP9gBKfYMyK40L5tfmJJnRNMJM/MpxxIS2cEnKYfnXGMqL2ZleAxkrT+G6sqNdQHETKHx0+gRe1PRMvdj7aXk7NW8BGpWwAm3k4geJ4vBf4ckp1GlmexuZNlTJqX75thCKBAoGADYSzQ4jGEzdO5ZLityWTDrZZU3XoWlmWNyBSYfQw164toAgnmdn2+t5vdfadEcSr5JXDsIANSX7A+wXWU6sNVvHcVIXIxNw8kt7goXkiDi/0UUx4j58hiFNEtMtI/IRzvHVsLAwbUcYnZr9GwgVZX1U5D+V4vvDYIionlJD7r20=");
        alipay.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy5OlvmyXl6pbQG8e7eNxJd1VB0b0cuZI1giLAjyNeP5Eu8AffsW+nbV+9gI7jppUfWNRbcgqM/Mx1G8NVE6OPr/1wUxTZm9ac/dEKwQSqi6KgfM101XVEo2Q1+PVks6GfdxB4ENTTwryokyn3LFz+cpqrdUXjQISDGRN4d7HEB2VxXP9bezPn6aHS2ADFYW1aIhiILJvdY7G9W8AoHgZ9Yhlfn+yVKi1AGA/c4+wINwkl731d+scC4WFsjdRjgcRxZ2uWTadwlhVRq799Slvd2k+ozHO56Gdd6itjDtFVdqIqWKM3V9PEHdBLbLjCcW0m+equhq7P6bRshjmitjzZwIDAQAB");
        alipay.setSignType(JanusProperties.SignType.RSA2);
        JanusProperties properties = new JanusProperties();
        properties.setAlipay(alipay);
        alipayService = new AlipayService(properties, restTemplate);
    }

    @Test
    void getToken() {
        long millis = 1584006120220L;
        AlipayGetTokenResponse.AlipaySystemOauthTokenResponse tokenResponse = new AlipayGetTokenResponse.AlipaySystemOauthTokenResponse();
        tokenResponse.setAccessToken("20120823ac6ffaa4d2d84e7384bf983531473993");
        tokenResponse.setExpiresIn("3600");
        tokenResponse.setRefreshToken("30120823ac6ffdsdf2d84e7384bf983531473994");
        tokenResponse.setUserId("2088102150477652");
        AlipayGetTokenResponse getTokenResponse = new AlipayGetTokenResponse();
        getTokenResponse.setAlipaySystemOauthTokenResponse(tokenResponse);
        ResponseEntity<AlipayGetTokenResponse> responseEntity = new ResponseEntity<>(getTokenResponse, HttpStatus.OK);
        given(restTemplate.getForEntity("https://openapi.alipay.com/gateway.do?app_id=2018060660309824&charset=UTF-8&code=123&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=RSA2&timestamp=" + DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss") + "&version=1.0&sign=blEwTT+O9w40ma7G6houkPTEgfh0VsC8q8xb/NzKMr3GJhyJylgfexGVDeCE7Pty7S4rwCExA5A+Ely/XGr3y20bp/FMrD2ee7SnIODJKCb2vrLqZ/lF4rxz/pF4MOcWcb49zV5WZFa0yXHB7YTN93Gpuk+wSTC3fbOkkLXZoc05G/ObuR44ZqapsG+GzvQJaDwRMW4KvqUpJ0m9VqDznDMPUrx2Yew3xmdqAWxxKKJmh+AOvIHwEwB3KMWMRQk0zymsI3IvBGvWI6wOrVxcEvGDFppcrH/5w8NUCWGG/4h7FVYCJX91v7MpkY+35vXnZ71ag1DSe0SgHdtCI+ruaQ==",
            AlipayGetTokenResponse.class))
            .willReturn(responseEntity);
        AlipayGetTokenResponse.AlipaySystemOauthTokenResponse response = alipayService.getToken("123", millis);
        assertEquals(tokenResponse, response);
    }

    @Test
    void getUser() {
        long millis = 1584006120220L;
        AlipayGetUserResponse.AlipayUserInfoShareResponse expectResponse = new AlipayGetUserResponse.AlipayUserInfoShareResponse();
        expectResponse.setCode("10000");
        expectResponse.setMsg("Success");
        expectResponse.setUserId("2088102104794936");
        expectResponse.setAvatar("http://tfsimg.alipay.com/images/partner/T1uIxXXbpXXXXXXXX");
        expectResponse.setProvince("安徽省");
        expectResponse.setCity("安庆");
        expectResponse.setNickName("支付宝小二");
        expectResponse.setGender("F");
        AlipayGetUserResponse getUserResponse = new AlipayGetUserResponse();
        getUserResponse.setAlipayUserInfoShareResponse(expectResponse);
        ResponseEntity<AlipayGetUserResponse> responseEntity = new ResponseEntity<>(getUserResponse, HttpStatus.OK);
        given(restTemplate.getForEntity("https://openapi.alipay.com/gateway.do?app_id=2018060660309824&auth_token=123456&charset=UTF-8&method=alipay.user.info.share&sign_type=RSA2&timestamp=" + DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss") + "&version=1.0&sign=K3O3hGFBQjYCKW9+aSWjPN1Ft5ZnqlgjR/nojjeYnf0Vo56nUkwMS+s/OlQPZEPbMysMRKH7e0CKLYBZDb9hF8xH3kGraxbOsGZheFnkWFT6bexPIoYlMVHvlOrm+JaDS5lPrFIC/+f3BoTjbgeyMVSGWbr6RBDuK8yfzW6CBmXMYtIht5E6OSLQ8SxJinayK5j9jC0SKyYsDLFiFC5KLE7Env7AJI60JD97YNfp0dpuPCXN9xeXP6PK9i26AHSmRb2gjJKQFq479yPhBxjNNe7ASZDN2eZtN4WcdB7g9O7Zhkr1Sw7l9d0K2EPlqnWMHy8n3zm+QxyjsZ/4W/s7kg==",
            AlipayGetUserResponse.class))
            .willReturn(responseEntity);
        AlipayGetUserResponse.AlipayUserInfoShareResponse actualResponse = alipayService.getUser("123456", millis);
        assertEquals(expectResponse, actualResponse);
    }

    @Test
    void sign() throws Exception {
        assertEquals("U9yg7nPnr2/KjETq480XI9h1HiYExfSyO2SG46FutO3JedFSWQ7QLPug4h94UvNTMAVpyxYYsjMrVR22MXUA+PL9nBvfOj3nl8TxSO/YyLpovZJ9/D4WKdKaZ7jN/ngbsxg7USFT6Z7B8MZ1F885SW+hWFhqp2OTMaOpZRdLGNFBOVWrN8Q87lwqYqv/B7nQnjSWyeev/oPhsMp67AT+NmYu3dNecN1dHhZzE5xmnHwRyCK3ubb3hmQaCyDbRZ55tIUKgz0efLcJUl0f8xkwhc2xHrFHJiyWAPOYTSueXzoNivIEZJua4lv12ceUDuLvrUcaJUyL94w+94KMuJWR+A==",
            alipayService.sign("app_id=2018060660309824&auth_token=123456&charset=UTF-8&method=alipay.user.info.share&sign_type=RSA2&timestamp=2020-03-11 21:17:32&version=1.0"));
        assertEquals("hhXi1Oe9ctU/H95LQ3Vluig4eD34dUMpocDwxxwqP6PokDmDHmQzMMXMAiZJEECecFX4lhMPt+5Iv+kKNyBYF9GNHUY6DKLUeLgddmobqzJiiPohLJj13/ocOPBxLnLHB1C7RNKyr8pZAtzNyHVhIjbd+5ImKfDasXMX/tyTVNJO7otWCgq73bXr61X5DWKllNqB4E3f33hVOc7DkQtMOFSTIVlFS3xrRS5KiqPdqAj1hNPobtt/N0bANOKZwcAFH+peRhRnWWVYoRcvnxg3q5ADgnkoky61Cnz6QPW8CbCTBAVCv7DazMwyGnb7my3jP7iCnPvTdz7astf2tV4hFQ==",
            alipayService.sign("app_id=2018060660309824&charset=UTF-8&code=123&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=RSA2&timestamp=2020-03-11 21:12:47&version=1.0"));
    }

    @Test
    void requestFailed() throws Exception {
        long millis = 1584006120220L;
        RestClientException cause = new RestClientException("Connect timeout", new SocketTimeoutException());
        given(restTemplate.getForEntity("https://openapi.alipay.com/gateway.do?app_id=2018060660309824&charset=UTF-8&code=123&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=RSA2&timestamp=" + DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss") + "&version=1.0&sign=blEwTT+O9w40ma7G6houkPTEgfh0VsC8q8xb/NzKMr3GJhyJylgfexGVDeCE7Pty7S4rwCExA5A+Ely/XGr3y20bp/FMrD2ee7SnIODJKCb2vrLqZ/lF4rxz/pF4MOcWcb49zV5WZFa0yXHB7YTN93Gpuk+wSTC3fbOkkLXZoc05G/ObuR44ZqapsG+GzvQJaDwRMW4KvqUpJ0m9VqDznDMPUrx2Yew3xmdqAWxxKKJmh+AOvIHwEwB3KMWMRQk0zymsI3IvBGvWI6wOrVxcEvGDFppcrH/5w8NUCWGG/4h7FVYCJX91v7MpkY+35vXnZ71ag1DSe0SgHdtCI+ruaQ==",
            AlipayGetTokenResponse.class))
            .willThrow(cause);
        try {
            alipayService.getToken("123", millis);
            fail("AlipayRequestException should be raised");
        } catch (AlipayRequestException ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.ALIPAY_REQUEST_FAILED);
            assertEquals(ex.getArgs()[0], cause);
        }
    }

    @Test
    void responseError() throws Exception {
        long millis = 1584006120220L;
        ResponseEntity<AlipayGetTokenResponse> responseEntity = new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        given(restTemplate.getForEntity("https://openapi.alipay.com/gateway.do?app_id=2018060660309824&charset=UTF-8&code=123&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=RSA2&timestamp=" + DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss") + "&version=1.0&sign=blEwTT+O9w40ma7G6houkPTEgfh0VsC8q8xb/NzKMr3GJhyJylgfexGVDeCE7Pty7S4rwCExA5A+Ely/XGr3y20bp/FMrD2ee7SnIODJKCb2vrLqZ/lF4rxz/pF4MOcWcb49zV5WZFa0yXHB7YTN93Gpuk+wSTC3fbOkkLXZoc05G/ObuR44ZqapsG+GzvQJaDwRMW4KvqUpJ0m9VqDznDMPUrx2Yew3xmdqAWxxKKJmh+AOvIHwEwB3KMWMRQk0zymsI3IvBGvWI6wOrVxcEvGDFppcrH/5w8NUCWGG/4h7FVYCJX91v7MpkY+35vXnZ71ag1DSe0SgHdtCI+ruaQ==",
            AlipayGetTokenResponse.class))
            .willReturn(responseEntity);
        try {
            alipayService.getToken("123", millis);
            fail("AlipayResponseException should be raised");
        } catch (AlipayResponseException ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.ALIPAY_RESPONSE_ERROR);
            assertEquals(ex.getArgs()[0], responseEntity);
        }
    }

    @Test
    void emptyResponseBody() throws Exception {
        long millis = 1584006120220L;
        given(restTemplate.getForEntity("https://openapi.alipay.com/gateway.do?app_id=2018060660309824&charset=UTF-8&code=123&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=RSA2&timestamp=" + DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss") + "&version=1.0&sign=blEwTT+O9w40ma7G6houkPTEgfh0VsC8q8xb/NzKMr3GJhyJylgfexGVDeCE7Pty7S4rwCExA5A+Ely/XGr3y20bp/FMrD2ee7SnIODJKCb2vrLqZ/lF4rxz/pF4MOcWcb49zV5WZFa0yXHB7YTN93Gpuk+wSTC3fbOkkLXZoc05G/ObuR44ZqapsG+GzvQJaDwRMW4KvqUpJ0m9VqDznDMPUrx2Yew3xmdqAWxxKKJmh+AOvIHwEwB3KMWMRQk0zymsI3IvBGvWI6wOrVxcEvGDFppcrH/5w8NUCWGG/4h7FVYCJX91v7MpkY+35vXnZ71ag1DSe0SgHdtCI+ruaQ==",
            AlipayGetTokenResponse.class))
            .willReturn(new ResponseEntity<>(HttpStatus.OK));
        try {
            alipayService.getToken("123", millis);
            fail("AlipayUnknownError should be raised");
        } catch (AlipayUnknownError ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.ALIPAY_UNKNOWN_ERROR);
        }
    }

    @Test
    void businessException() throws Exception {
        long millis = 1584006120220L;
        AlipayBaseResponse.ErrorResponse errorResponse = new AlipayBaseResponse.ErrorResponse();
        errorResponse.setCode("20000");
        errorResponse.setMsg("Service Currently Unavailable");
        errorResponse.setSubCode("isp.unknow-error");
        errorResponse.setSubMsg("系统繁忙");
        AlipayGetTokenResponse response = new AlipayGetTokenResponse();
        response.setErrorResponse(errorResponse);
        given(restTemplate.getForEntity("https://openapi.alipay.com/gateway.do?app_id=2018060660309824&charset=UTF-8&code=123&grant_type=authorization_code&method=alipay.system.oauth.token&sign_type=RSA2&timestamp=" + DateFormatUtils.format(millis, "yyyy-MM-dd HH:mm:ss") + "&version=1.0&sign=blEwTT+O9w40ma7G6houkPTEgfh0VsC8q8xb/NzKMr3GJhyJylgfexGVDeCE7Pty7S4rwCExA5A+Ely/XGr3y20bp/FMrD2ee7SnIODJKCb2vrLqZ/lF4rxz/pF4MOcWcb49zV5WZFa0yXHB7YTN93Gpuk+wSTC3fbOkkLXZoc05G/ObuR44ZqapsG+GzvQJaDwRMW4KvqUpJ0m9VqDznDMPUrx2Yew3xmdqAWxxKKJmh+AOvIHwEwB3KMWMRQk0zymsI3IvBGvWI6wOrVxcEvGDFppcrH/5w8NUCWGG/4h7FVYCJX91v7MpkY+35vXnZ71ag1DSe0SgHdtCI+ruaQ==",
            AlipayGetTokenResponse.class))
            .willReturn(new ResponseEntity<>(response, HttpStatus.OK));
        try {
            alipayService.getToken("123", millis);
            fail("AlipayBusinessException should be raised");
        } catch (AlipayBusinessException ex) {
            assertEquals(ex.getCode(), AuthenticationExceptionWithCode.ErrorCode.ALIPAY_BUSINESS_EXCEPTION);
            assertEquals(ex.getArgs()[0], "20000");
            assertEquals(ex.getArgs()[1], "Service Currently Unavailable");
            assertEquals(ex.getArgs()[2], "isp.unknow-error");
            assertEquals(ex.getArgs()[3], "系统繁忙");
        }
    }
}
