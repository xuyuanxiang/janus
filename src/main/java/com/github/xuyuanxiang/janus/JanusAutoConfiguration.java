package com.github.xuyuanxiang.janus;

import com.github.xuyuanxiang.janus.custom.CustomAuthorizationConfiguration;
import com.github.xuyuanxiang.janus.service.*;
import com.github.xuyuanxiang.janus.web.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ForwardedHeaderFilter;

public class JanusAutoConfiguration extends WebSecurityConfigurerAdapter {
    @Bean
    @ConfigurationProperties(prefix = "janus")
    JanusProperties properties() {
        return new JanusProperties();
    }

    @Bean
    RestTemplate janusRestTemplate(JanusProperties properties) {
        return new RestTemplateBuilder()
            .setConnectTimeout(properties.getConnectionTimeout())
            .setReadTimeout(properties.getReadTimeout())
            .messageConverters(new JanusJackson2HttpMessageConverter())
            .build();
    }

    @Bean
    FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        final FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<ForwardedHeaderFilter>();

        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return filterRegistrationBean;
    }

    @Bean
    AlipayService alipayService(JanusProperties properties, RestTemplate janusRestTemplate) {
        return new AlipayService(properties, janusRestTemplate);
    }

    @Bean
    WechatService wechatService(JanusProperties properties, RestTemplate janusRestTemplate) {
        return new WechatService(properties, janusRestTemplate);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        ApplicationContext context = getApplicationContext();
        JanusProperties properties = context.getBean(JanusProperties.class);
        AlipayService alipayService = context.getBean(AlipayService.class);
        WechatService wechatService = context.getBean(WechatService.class);
        FindByIndexNameSessionRepository sessionRepository = null;
        try {
            sessionRepository = context.getBean(FindByIndexNameSessionRepository.class);
        } catch (Exception ignored) {

        }

        if (sessionRepository != null) {
            http.sessionManagement()
                .maximumSessions(1)
                .sessionRegistry(new SpringSessionBackedSessionRegistry(sessionRepository));
        }

        http
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .rememberMe().disable()
            .headers()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.ORIGIN)
            .and()
            .frameOptions().disable()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(new JanusEntryPoint(properties))
            .accessDeniedHandler(new JanusAccessDeniedHandler(properties))
            .and()
            .authorizeRequests()
            .antMatchers(properties.getDeniedUrl(), properties.getFailureUrl(), properties.getLogoutSuccessUrl())
            .permitAll()
            .and()
            .logout()
            .logoutUrl(properties.getLogoutRequestUrl())
            .logoutSuccessHandler(new JanusLogoutSuccessHandler(properties))
            .and()
            .addFilterBefore(new WechatOAuthCallbackFilter(properties, wechatService),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new AlipayOAuthCallbackFilter(properties, alipayService), WechatOAuthCallbackFilter.class)
        ;

        try {
            CustomAuthorizationConfiguration customAuthorizationConfiguration = getApplicationContext().getBean(CustomAuthorizationConfiguration.class);
            if (customAuthorizationConfiguration != null) {
                customAuthorizationConfiguration.customize(http);
            }
        } catch (Exception ignored) {
            http.authorizeRequests().anyRequest().authenticated();
        }

    }
}
