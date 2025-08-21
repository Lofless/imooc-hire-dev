package com.imooc.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.bo.RegistLoginBO;
import com.imooc.result.GraceJSONResult;
import com.imooc.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/passport")
@Slf4j
public class PassportController extends BaseInfoProperties {

    @GetMapping("/getSMSCode")
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) {

        // 判断是否传输mobile
        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.error();
        }

        // 获取用户IP
        String userIp = IPUtil.getRequestIp(request);
        // 限制用户只能在60s以为获得一次验证码，限制发送的业务代码已经放在了拦截器里面
        redis.setnx60s(MOBILE_SMSCODE+":"+userIp, mobile);

        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        // 不真实发送
        log.info("验证码SMSCode为:{}",code);
        // 验证码存入redis中，并设置过期时间
        redis.set(MOBILE_SMSCODE+":"+mobile, code,30*60);
        return GraceJSONResult.ok();
    }

    @PostMapping("/login")
    public GraceJSONResult getSMSCode(@Validated @RequestBody RegistLoginBO registLoginBO, HttpServletRequest request) {

        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSMSCode();



        return GraceJSONResult.ok();
    }

}
