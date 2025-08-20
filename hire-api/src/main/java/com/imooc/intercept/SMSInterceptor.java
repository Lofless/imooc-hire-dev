package com.imooc.intercept;

import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器
 */
@Slf4j
public class SMSInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        log.info("进入拦截器preHandle中，请求URI={}", request.getRequestURI());

        // 获取用户IP
        String userIp = IPUtil.getRequestIp(request);
        // 获取用于判断的boolean值
        boolean ipExists = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);

        if(ipExists){
            log.error("短信发送频率过快～～请稍后重试");
            return false;
        }

        /**
         * false：请求被拦截
         * true：放行，请求验证通过
         */
        return true;
    }

}
