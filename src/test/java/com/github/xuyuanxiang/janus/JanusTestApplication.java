package com.github.xuyuanxiang.janus;

import com.github.xuyuanxiang.janus.custom.CustomAuthorizationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@SpringBootApplication
class JanusTestApplication {
    @Bean
    CustomAuthorizationConfiguration customAuthorizationConfiguration() {
        return httpSecurity -> httpSecurity.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/protected").hasRole("ADMIN")
            .anyRequest().authenticated();
    }

    public static void main(String[] args) {
        SpringApplication.run(JanusTestApplication.class, args);
    }
}
