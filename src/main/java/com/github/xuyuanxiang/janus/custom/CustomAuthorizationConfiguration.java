package com.github.xuyuanxiang.janus.custom;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface CustomAuthorizationConfiguration {
    void customize(HttpSecurity httpSecurity);
}
