package com.imooc.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class SecurityFilterJWT implements GlobalFilter, Ordered {

    @Autowired
    private ExcludeUrlProperties excludeUrlProperties;

    // 路径规则匹配器
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.获取当前的请求路径
        String url = exchange.getRequest().getURI().getPath();
        log.info("url:{}", url);

        // 2.获取所有的需要排除校验的url list
        List<String> excludeUrlList = excludeUrlProperties.getUrls();

        // 3.校验并且排除excludeList
        if(excludeUrlList != null || !excludeUrlList.isEmpty()) {
            for(String excludeUrl : excludeUrlList) {

                if(antPathMatcher.matchStart(excludeUrl, url)){
                    // 如果匹配到，就直接放行，表示不需要拦截
                    return chain.filter(exchange);
                }

            }
        }

        log.info("被拦截了～～");

    }

    // 过滤器额顺序，数字越小优先级越大
    @Override
    public int getOrder() {
        return 0;
    }
}
