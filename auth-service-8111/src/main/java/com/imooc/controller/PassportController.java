package com.imooc.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.imooc.base.BaseInfoProperties;
import com.imooc.result.GraceJSONResult;
import com.imooc.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("passport")
@Slf4j
public class PassportController extends BaseInfoProperties {

    @GetMapping("getSMSCode")
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) {

        // 判断是否传输mobile
        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.error();
        }

        // 获取用户IP
        String userIp = IPUtil.getRequestIp(request);
        // 限制用户只能在60s以为获得一次验证码
        redis.setnx60s(MOBILE_SMSCODE+":"+userIp, mobile);

        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        // 不真实发送
        log.info("验证码SMSCode为:{}",code);
        // 验证码存入redis中，并设置过期时间
        redis.set(MOBILE_SMSCODE+":"+mobile, code,30*60);
        return GraceJSONResult.ok();
    }

}
