package com.imooc.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.imooc.base.BaseInfoProperties;
import com.imooc.result.GraceJSONResult;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class IPLimitFilter extends BaseInfoProperties implements GlobalFilter, Ordered {

    @Autowired
    private ExcludeUrlProperties excludeUrlProperties;

    // 路径规则匹配器
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${blackIP.continueCounts}")
    private Integer continueCounts;
    @Value("${blackIP.timeInterval}")
    private Integer timeInterval;
    @Value("${blackIP.limitTimes}")
    private Integer limitTimes;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.获取当前的请求路径
        String url = exchange.getRequest().getURI().getPath();

        // 2. 获得所有的需要进行ip限流校验的url list
        List<String> ipLimitList = excludeUrlProperties.getIpLimitUrls();

        // 3. 校验
        if(ipLimitList != null && !ipLimitList.isEmpty()) {
            for(String limitUrl : ipLimitList) {
                if(antPathMatcher.matchStart(limitUrl, url)) {
                    // 如果匹配到，则表明需要进行ip的拦截校验
                    log.info("IPLimitFilter - 拦截到需要进行ip限流校验的方法：URL = {}", url);
                    return doLimit(exchange, chain);
                }
            }
        }

        // 4. 默认直接放行
        return chain.filter(exchange);
    }


    /**
     * 需要进行ip的拦截校验
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> doLimit(ServerWebExchange exchange, GatewayFilterChain chain){

        // 根据request获得请求ip
        ServerHttpRequest request = exchange.getRequest();
        String ip = IPUtil.getIP(request);

        /**
         * 需求；
         * 判断ip在[timeInterval秒]内的请求次数是否超过了[continueCounts次]
         * 如果超过，那么就需要静默[limitTimes秒]
         */

        // 正常的ip
        final String ipRedisKey = "gateway-ip:" + ip;
        // 被拦截的黑名单，如果存在，则表示目前被关小黑屋
        final String ipReadIsLimitedKey = "gateway-ip:limit:" + ip;

        // 获得当前ip，查询是否在小黑屋中，如果在小黑屋中那么就直接拦截请求
        long ttl = redis.ttl(ipReadIsLimitedKey);

        if(ttl > 0){
            // 中止请求，返回错误
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }

        // 在redis中获得ip的累加次数
        long requestCounts = redis.increment(ipRedisKey, 1);
        // 判断如果是第一次进来，那么初期访问就是1，需要设置间隔的时间，也就是连续请求的间隔时间
        if(requestCounts == 1){
            redis.expire(ipRedisKey, timeInterval);
        }

        // 如果取得请求的次数，说明用户的连续请求落在限定时间内，则需要限制当前ip
        if(requestCounts > continueCounts) {
            // 限制ip访问
            redis.set(ipReadIsLimitedKey, ipReadIsLimitedKey, limitTimes);
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }

        return chain.filter(exchange);

    }

    /**
     * 重新包装并且返回错误信息
     * @param exchange
     * @param statusEnum
     * @return
     */
    public Mono<Void> renderErrorMsg(ServerWebExchange exchange,
                                     ResponseStatusEnum statusEnum) {
        // 1. 获得response
        ServerHttpResponse response = exchange.getResponse();

        // 2. 构建jsonResult
        GraceJSONResult jsonResult = GraceJSONResult.exception(statusEnum);

        // 3. 修改response的code为500错误
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        // 4. 设定hearder类型
        if (!response.getHeaders().containsKey("Content-Type")) {
            response.getHeaders().add("Content-Type", MimeTypeUtils.APPLICATION_JSON_VALUE);
        }

        // 5. 转换json并且向response写入数据
        String json = JSONObject.toJSON(jsonResult).toString();
        DataBuffer dataBuffer = response
                .bufferFactory()
                .wrap(json.getBytes());

        return response.writeWith(Mono.just(dataBuffer));
    }

    // 过滤器额顺序，数字越小优先级越大
    @Override
    public int getOrder() {
        return 1;
    }
}
