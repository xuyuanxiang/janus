# janus-server-sdk

[![Download](https://api.bintray.com/packages/freeman/janus-server-sdk/com.github.xuyuanxiang%3Ajanus-server-sdk/images/download.svg) ](https://bintray.com/freeman/janus-server-sdk/com.github.xuyuanxiang%3Ajanus-server-sdk/_latestVersion)
[![codecov](https://codecov.io/gh/xuyuanxiang/janus-server-sdk/branch/master/graph/badge.svg)](https://codecov.io/gh/xuyuanxiang/janus-server-sdk)
[![Build Status](https://travis-ci.org/xuyuanxiang/janus-server-sdk.svg?branch=master)](https://travis-ci.org/xuyuanxiang/janus-server-sdk)

基于 spring-security 和 spring-session 封装的微信和支付宝用户网页授权（OAuth）客户端，项目遵循 spring-boot 自动装配的风格，引入依赖后做一些简单的中间件和授权参数的配置即可。

## 目录

+ [业务规则](#业务规则)
+ [参数配置](#参数配置)
+ [自定义角色/权限](#自定义角色/权限)
+ [获取当前用户信息](#获取当前用户)
+ [HTTPS配置](#HTTPS配置)

## 业务规则

![](doc/sequence.svg)

+ [微信用户网页授权](https://mp.weixin.qq.com/wiki?action=doc&id=mp1421140842&t=0.888455262701805)
+ [支付宝用户网页授权](https://docs.open.alipay.com/53/104114)

## 参数配置

application.yml示例：

```yaml
spring:
  session:
    timeout: 10m
janus:
  alipay:
    app-id: APPID
    sign-type: RSA2
    private-key: PRIVATE—KEY
  wechat:
    appid: APPID
    secret: SECRET
```

以上是最少配置，其他配置及说明详见：[JanusProperties.java](src/main/java/com/github/xuyuanxiang/janus/JanusProperties.java)注释。

### 使用Redis存储session

第一步，添加依赖：
```xml
<dependencies>
    <dependency>
        <groupId>com.github.xuyuanxiang</groupId>
        <artifactId>janus-server-sdk</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.session</groupId>
        <artifactId>spring-session-data-redis</artifactId>
    </dependency>
</dependencies>       
```

第二步，配置连接参数：

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 32
        max-idle: 8
        max-wait: 2s
    timeout: 600ms
    host: localhost
    port: 6379
    password: mypassword
    database: 11
  session:
    redis:
      namespace: myApp:session
    timeout: 2h
janus:
  alipay:
    app-id: APPID
    sign-type: RSA2
    private-key: PRIVATE—KEY
  wechat:
    appid: APPID
    secret: SECRET
```

[spring-session](https://spring.io/projects/spring-session)文档中查看更多session存储方式。

### 自定义角色/权限

默认情况下，用户使用支付宝或微信在未登录情况下访问任意路径，都会跳转到相应的授权页面，待用户同意授权后会返回未登录前所访问的路径。

注入一个Bean实现自定义：

```java
import com.github.xuyuanxiang.janus.custom.CustomAuthorizationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class MyConfiguration {
    @Bean
    CustomAuthorizationConfiguration customAuthorizationConfiguration() {
        // Expression-Based Access Control
        return httpSecurity -> httpSecurity.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/protected", "/ping").permitAll()
            .anyRequest().authenticated();
    }
}

```

spring-security文档中有详细的使用方式：

+ [Expression-Based Access Control](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#el-access)
+ 除了上面代码示例中的方式，还可以使用[Method Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#jc-method)

### 获取当前用户

```java
import com.github.xuyuanxiang.janus.model.JanusAuthentication;
import com.github.xuyuanxiang.janus.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class MyController {
    @GetMapping
    public User getUser() {
        JanusAuthentication authentication = (JanusAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getUser();
    }
}
```

属性详见[User类](src/main/java/com/github/xuyuanxiang/janus/model/User.java)。

[JanusAuthentication类](src/main/java/com/github/xuyuanxiang/janus/model/JanusAuthentication.java)储存了access_token及其有效期，创建时间，授权方式等信息。

## HTTPS配置

### 为什么HTTPS在授权登录后总是跳转到HTTP？

使用Nginx或云服务（比如：阿里云SLB）做代理转发时，只要有以下请求头之一：

```yaml
Forwarded: proto=https;host=spa.shouqianba.com
X-Forwarded-Proto: https
X-Forwarded-Ssl: on
```

spring-security 就"认为"原始请求是一个HTTPS请求，参考文献：[https://tools.ietf.org/html/rfc7239](https://tools.ietf.org/html/rfc7239)。

请确保代理转发时添加了以上**任意一个请求头**。

阿里云SLB勾选红箭头所示选项：

![](doc/slb.png)

Nginx：

```text
server {
  # 其他配置略
  location / {
    # 其他配置略
    proxy_set_header X-Forwarded-Proto $scheme;
    # 或者：
    # proxy_set_header X-Forwarded-Ssl on;
  }
}
```
