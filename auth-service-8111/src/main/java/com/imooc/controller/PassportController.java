package com.imooc.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.RegistLoginBO;
import com.imooc.result.GraceJSONResult;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.service.UsersService;
import com.imooc.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/passport")
@Slf4j
public class PassportController extends BaseInfoProperties {

    @Autowired
    private UsersService usersService;

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
        String code = registLoginBO.getCode();

        // 1, 从redis中获得验证码进行校验匹配
        String SMSCode = redis.get(MOBILE_SMSCODE+":"+mobile);
        if(StringUtils.isBlank(SMSCode) || !code.equalsIgnoreCase(SMSCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2.根据mobile查询数据库，判断用户是否存在
        usersService.queryMobileIsExist(mobile);
        Users user = usersService.queryMobileIsExist(mobile);
        if(user == null){
            // 2.1 如果查询的用户为空，则表示没有注册过，这需要注册信息入库
            user = usersService.createUsers(mobile);
        }

        // 3. 用户登录注册后，删除redis中的短信验证码
        redis.del(MOBILE_SMSCODE+":"+mobile);

        // 4.返回用户的信息给前端
        return GraceJSONResult.ok(user);
    }

}
