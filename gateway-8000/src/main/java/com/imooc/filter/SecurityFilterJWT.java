package com.imooc.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.imooc.result.GraceJSONResult;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class SecurityFilterJWT implements GlobalFilter, Ordered {

    public static final String HEADER_USER_TOKEN = "headerUserToken";

    @Autowired
    private ExcludeUrlProperties excludeUrlProperties;

    @Autowired
    private JWTUtils jwtUtils;

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

        // 判断hearder中是否有token
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String userToken = headers.getFirst(HEADER_USER_TOKEN);
        log.info("userToken:{}", userToken);

        if(StringUtils.isNotBlank(userToken)){
            String[] tokenArr = userToken.split(JWTUtils.at);
            if(tokenArr.length < 2) {
                return renderErrorMsg(exchange, ResponseStatusEnum.UN_LOGIN);
            }

            // 获得jwt令牌和前缀
            String prefix = tokenArr[0];
            String jwt = tokenArr[1];

            // 解析校验
            return dealJWT(jwt, exchange, chain);
        }


        log.error("被拦截了～～");
        // 不放行，jwt校验出错了
//        GraceException.display(ResponseStatusEnum.UN_LOGIN);
//        return chain.filter(exchange);
        return renderErrorMsg(exchange,ResponseStatusEnum.UN_LOGIN);
    }

    public Mono<Void> dealJWT(String jwt, ServerWebExchange exchange, GatewayFilterChain chain){
        try{
            String userJson = jwtUtils.checkJWT(jwt);
            return chain.filter(exchange);
        }catch (ExpiredJwtException e){
            e.printStackTrace();
            return renderErrorMsg(exchange, ResponseStatusEnum.JWT_EXPIRE_ERROR);
        }catch (Exception e){
            e.printStackTrace();
            return renderErrorMsg(exchange, ResponseStatusEnum.JWT_SIGNATURE_ERROR);
        }
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
        return 0;
    }
}
