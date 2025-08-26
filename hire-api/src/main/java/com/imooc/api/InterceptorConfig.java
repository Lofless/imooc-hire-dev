package com.imooc.api;

import com.imooc.intercept.JWTCurrentUserInterceptor;
import com.imooc.intercept.SMSInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    public InterceptorConfig() {
        log.info("InterceptorConfig 加载成功！");
    }

    /**
     * 在springboot容器中放入拦截器
     * @return
     */
    @Bean
    public SMSInterceptor smsInterceptor() {
        return new SMSInterceptor();
    }

    @Bean
    public JWTCurrentUserInterceptor jwtInterceptor() {
        return new JWTCurrentUserInterceptor();
    }

    /**
     * 注册拦截器，并且拦截指定的路由，否则不生效
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("拦截器注册中...");
        registry.addInterceptor(smsInterceptor())
                .addPathPatterns("/passport/getSMSCode");

        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**");
    }

}
