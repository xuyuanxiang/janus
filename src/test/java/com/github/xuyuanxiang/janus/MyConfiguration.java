package com.github.xuyuanxiang.janus;

import com.github.xuyuanxiang.janus.custom.CustomAuthorizationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class MyConfiguration {
    @Bean
    CustomAuthorizationConfiguration customAuthorizationConfiguration() {
        return httpSecurity -> httpSecurity.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/protected", "/ping").permitAll()
            .anyRequest().authenticated();
    }
}
