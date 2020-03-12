package com.github.xuyuanxiang.janus;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.time.Duration;

@Slf4j
@Data
public class JanusProperties {
    public static final String ALIPAY_AUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&response_type=code&scope=auth_base&redirect_uri=%s&state=%s";
    public static final String WECHAT_AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=%s#wechat_redirect";
    public static final String SAVED_REQUEST = "JANUS_SAVED_REQUEST";
    public static final String OAUTH_CALLBACK_URL = "/oauth/callback";

    /**
     * 支付宝授权参数
     */
    private AlipayConfiguration alipay;
    /**
     * 微信授权参数
     */
    private WechatConfiguration wechat;
    /**
     * 授权过程中发生系统异常时，在3次重试失败之后放弃重试，并且accept字段非application/json时，会返回HTTP 302 重定向到该路由。 您可以在此路由下返回一个对用户友好的HTML错误页。
     */
    private String failureUrl = "/500";
    /**
     * 权限不足，并且HTTP请求头accept字段非application/json时，会返回HTTP 302 重定向到该路由。您可以在此路由下返回一个对用户友好的HTML 403页面。
     */
    private String deniedUrl = "/403";
    /**
     * 用户没有在支付宝App或者微信中访问时，会返回HTTP 302 重定向到该路由。您可以在此路由下返回一个对用户友好的HTML 401页面。
     */
    private String fallbackUrl = "/401";
    /**
     * 授权成功后，"强制"用户访问的路由。
     * 通常无需设置，除非有特殊需求，比如：每次session失效或者未登录时访问应用，在授权成功后必须强制用户访问首页。
     * 缺省情况下，会保存未授权时用户所访问的路由，在授权成功后继续访问该路由。
     */
    private String successUrl;
    /**
     * 供您的应用内发起【退出登录】操作的请求路径，在需要销毁session时，应到用户跳转到该路由即可。
     */
    private String logoutRequestUrl = "/logout";
    /**
     * 【退出登录】成功后，引导用户返回该路由。
     */
    private String logoutSuccessUrl = "/";
    /**
     * 支付宝/微信 接口请求超时时间，缺省值：5秒。超过该时间还未成功建立连接则直接放弃。通常在首次失败后，会最多再重试3次。
     */
    private Duration connectionTimeout = Duration.ofSeconds(5);
    /**
     * 支付宝/微信 接口响应超时时间，缺省值：30秒。超过该时间接口仍未响应则直接放弃。 通常在首次失败后，会最多再重试3次。
     */
    private Duration readTimeout = Duration.ofSeconds(30);
    /**
     * 是否启用Remember Me功能，免授权，直接登录。
     * 推荐不依赖access_token的业务可以开启该功能。
     */
    private boolean enableRememberMe;
    /**
     * Remember Me功能保存期限，缺省值：7天。
     * 即用户首次授权成功后，会在客户端本地保存一个为期7天的安全cookie。此后只要该cookie存在，则直接以此直接登录，创建session。
     */
    private Duration rememberMeTTL = Duration.ofDays(7);

    @Data
    public static class AlipayConfiguration {
        /**
         * 支付宝应用ID。
         */
        private String appId;
        /**
         * 应用证书私钥，支付宝开放平台开发者在本地创建的证书私钥（公钥上传到了支付宝）。
         */
        private String privateKey;
        /**
         * 支付宝证书公钥，开发者从支付宝开放平台下载的（由支付宝生成的）证书公钥。
         */
        private String publicKey;
        /**
         * 加密方式，可选值：RSA，RSA2。 RSA即："SHA1WithRSA"，RSA2即："SHA256WithRSA"。
         */
        private SignType signType;

        public String getAppId() {
            Assert.notNull(appId, "\"janus.alipay.appId\" cannot be null.");
            return appId;
        }

        public String getPrivateKey() {
            Assert.notNull(privateKey, "\"janus.alipay.privateKey\" cannot be null.");
            return privateKey;
        }

        public String getPublicKey() {
            Assert.notNull(publicKey, "\"janus.alipay.publicKey\" cannot be null.");
            return publicKey;
        }

        public SignType getSignType() {
            Assert.notNull(signType, "\"janus.alipay.signType\" cannot be null.");
            return signType;
        }
    }

    public enum SignType {
        /**
         * SHA1WithRSA
         */
        RSA,
        /**
         * SHA256WithRSA
         */
        RSA2
    }

    @Data
    public static class WechatConfiguration {
        /**
         * 微信公众号 appid
         */
        private String appid;
        /**
         * 微信公众号 secret
         */
        private String secret;

        public String getAppid() {
            Assert.notNull(appid, "\"janus.wechat.appid\" cannot be null.");
            return appid;
        }

        public String getSecret() {
            Assert.notNull(secret, "\"janus.wechat.secret\" cannot be null.");
            return secret;
        }
    }
}
