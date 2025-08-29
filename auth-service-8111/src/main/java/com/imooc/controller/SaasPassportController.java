package com.imooc.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.SaasUserVO;
import com.imooc.result.GraceJSONResult;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.service.UsersService;
import com.imooc.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/saas")
@Slf4j
public class SaasPassportController extends BaseInfoProperties {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UsersService usersService;

    /**
     * 1. 获得二维码token令牌
     * @return
     */
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

    /**
     * 2. 手机端使用HR角色进行扫码操作
     * @param qrToken
     * @return
     */
    @PostMapping("scanCode")
    public GraceJSONResult scanCode(String qrToken, HttpServletRequest request) {

        // 如果没有qrToken直接返回错误消息
        if(StringUtils.isBlank(qrToken)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
        }

        String redisQRToken = redis.get(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken);
        if(!redisQRToken.equalsIgnoreCase(qrToken)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FAILED);
        }

        // 从header中获得用户id和jwt令牌
        String headerUserId = request.getHeader("appUserId");
        String headerUserToken = request.getHeader("appUserToken");

        // 判空
        if(StringUtils.isBlank(headerUserId) || StringUtils.isBlank(headerUserToken))
            return GraceJSONResult.errorCustom(ResponseStatusEnum.HR_TICKET_INVALID);

        String userJson = jwtUtils.checkJWT(headerUserToken.split("@")[1]);
        if(StringUtils.isBlank(userJson))
            return GraceJSONResult.errorCustom(ResponseStatusEnum.HR_TICKET_INVALID);

        // 执行后续正常业务
        // 生成预登录token
         String preToken = UUID.randomUUID().toString();
         redis.set(SAAS_PLATFORM_LOGIN_TOKEN + ":" + qrToken, preToken, 5 * 60);

         // redis写入标记
        redis.set(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken, "1," + preToken, 5 * 60);

        return GraceJSONResult.ok(preToken);

    }

    /**
     * 3. saas网页端在每隔一段时间（3s）定时查询qrToken是否被读取，用于页面是否被扫码的展示标记判断
     * 前端处理：限制用户在页面不操作而频繁发起调用：【页面失效，请刷新后再执行扫码登录】
     * @param qrToken
     * @return
     */
    @PostMapping("codeHasBeenRead")
    public GraceJSONResult codeHasBeenRead(String qrToken) {

        String readStr = redis.get(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken);
        List list = new ArrayList();

        if(StringUtils.isNotBlank(readStr)) {
            String[] readArr =  readStr.split(",");
            if(readArr.length >= 2) {
                list.add(Integer.valueOf(readArr[0]));
                list.add(readArr[1]);
            } else {
                list.add(0);
            }

            return GraceJSONResult.ok(list);
        } else {
            return GraceJSONResult.ok(list);
        }

    }

    /**
     * 4. 手机端点击登录，携带preToken与后端进行判断，如果校验ok则登录成功
     * 注：如果使用websocket或netty，可以在此直接通信H5进行页面跳转
     * @param userId
     * @param qrToken
     * @param preToken
     * @return
     */
    @PostMapping("goQRLogin")
    public GraceJSONResult goQRLogin(String userId,
                                     String qrToken,
                                     String preToken) {

        // 获得标记的preToken
        String preTokenRedisArr = redis.get(SAAS_PLATFORM_LOGIN_TOKEN_READ + ":" + qrToken);

        if(StringUtils.isNotBlank(preToken)) {
            String preTokenRedis = preTokenRedisArr.split(",")[1];
            if(preTokenRedis.equals(preToken)) {
                // 根据用户id获得用户信息
                Users hrUser = usersService.getById(userId);
                if(hrUser == null) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
                }

                // 存入用户信息到redis中，因为H5在未登录的情况下，拿不到用户ID，如果使用websocket是可以直接通信H5获得用户id，则无此问题
                // 当前接口不使用，在checkLogin接口用来判断是否已经登录
                redis.set(REDIS_SAAS_USER_INFO + ":temp:" + preToken, JSONObject.toJSON(hrUser).toString(), 5* 60 );
            }
        }

        return GraceJSONResult.ok();

    }


    /**
     * 5.检查登录
     * @param preToken
     * @return
     */
    @PostMapping("checkLogin")
    public GraceJSONResult checkLogin (String preToken) {

        if(StringUtils.isBlank(preToken)) {
            return GraceJSONResult.ok("");
        }

        // 获得用户的临时信息
        String userJson = redis.get(REDIS_SAAS_USER_INFO + ":temp:" + preToken);
        if(StringUtils.isBlank(userJson)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 确认执行登陆后，生成saas用户的token，并且长期有效，这边不设置时间有效期了
        String saasUserToken = jwtUtils.createJWTWithPrefix(userJson, TOKEN_SAAS_PREFIX);

        // 存入用户信息长期有效
        redis.set(REDIS_SAAS_USER_INFO + ":" + saasUserToken, userJson);
        return GraceJSONResult.ok(saasUserToken);

    }

    @GetMapping("info")
    public GraceJSONResult info(String token) {
        String userJson = redis.get(REDIS_SAAS_USER_INFO + ":" + token);
        Users sassUser = JSONObject.parseObject(userJson, Users.class);
        SaasUserVO saasUserVO = new SaasUserVO();
        BeanUtils.copyProperties(sassUser, saasUserVO);

        return GraceJSONResult.ok(saasUserVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(String token) {
        return GraceJSONResult.ok();
    }

}
