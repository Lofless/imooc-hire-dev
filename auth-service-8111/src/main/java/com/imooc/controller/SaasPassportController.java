package com.imooc.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.RegistLoginBO;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.result.GraceJSONResult;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.service.UsersService;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/saas")
@Slf4j
public class SaasPassportController extends BaseInfoProperties {

    @PostMapping("getQRToken")
    public GraceJSONResult getQRToken() {

        // 生成扫码登录的token
        String qrToken = UUID.randomUUID().toString();
        // 把qrToken存入到redis，设定过期时间
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken, qrToken, 5 * 60);
        // 存入redis标记当前的qrToken未被扫描
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken, "0", 5 * 60);

        // 返回给前端，让前端下一次请求的时候需要带上qrToken
        return GraceJSONResult.ok(qrToken);

    }

}
