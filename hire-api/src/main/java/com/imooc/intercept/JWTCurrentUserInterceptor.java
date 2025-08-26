package com.imooc.intercept;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Admin;
import com.imooc.pojo.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class JWTCurrentUserInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    public static ThreadLocal<Users> currentUser = new ThreadLocal<>();
    public static ThreadLocal<Admin> adminUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 使用ThreadLocal可以在同一个线程内共享数据
        // 比如：Interceptor --> 控制器Controller --> 业务层Service --> 数据层Mapper(Dao)

        String userJson = request.getHeader(APP_USER_JSON);
        String saasUserJson = request.getHeader(SAAS_USER_JSON);
        String adminUserJson = request.getHeader(ADMIN_USER_JSON);

        if(StringUtils.isNotBlank(userJson)
                || StringUtils.isNotBlank(saasUserJson)) {
            Users appUser = JSON.parseObject(userJson, Users.class);
            currentUser.set(appUser);
        }

        if(StringUtils.isNotBlank(adminUserJson)) {
            Admin admin = JSON.parseObject(adminUserJson, Admin.class);
            adminUser.set(admin);
        }

        /**
         * false：请求被拦截
         * true：放行，请求验证通过
         */
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        currentUser.remove();
        adminUser.remove();
    }

}
