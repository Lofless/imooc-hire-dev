package com.imooc.utils;

import com.imooc.exceptions.GraceException;
import com.imooc.result.ResponseStatusEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JWTUtils {

    @Autowired
    private JWTProperties jwtProperties;

    public String createJWTWithPrefix(String body, String prefix){
        return prefix + "@" +  dealJWT(body, null);
    }

    public String createJWTWithPrefix(String body, Long expire, String prefix){
        if (expire == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_NO_EXPIRE_ERROR);
        }
        return prefix + "@" + dealJWT(body, expire);
    }

    public String createJWT(String body){
        return dealJWT(body, null);
    }

    public String createJWT(String body, Long expire){
        if (expire == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_NO_EXPIRE_ERROR);
        }
        return dealJWT(body, expire);
    }

    public String dealJWT(String body, Long expire) {

        String userKey = jwtProperties.getKey();
        // 对密钥进行base64编码
        String base64 = new BASE64Encoder().encode(userKey.getBytes());
        // 对base64生成一个密钥对象
        SecretKey secretKey = Keys.hmacShaKeyFor(base64.getBytes());

        String jwt = "";
        if(expire != null){
            jwt = generatorJWT(body, secretKey, expire);
        }else {
            jwt = generatorJWT(body, secretKey);
        }

        log.info("JWTUtils - dealJWT : generatorJWT:{}", jwt);
        return jwt;
    }

    /**
     * 基础生成jwt方式
     * @param body 设置用户自定义数据，一般都是User类转换为Json格式的形式
     * @param secretKey 使用密钥对象进行jwt的生成
     * @return
     */
    public String generatorJWT(String body, SecretKey secretKey) {

        String myJWT = Jwts.builder()
                .setSubject(body)
                .signWith(secretKey)
                .compact(); // 压缩并且生成jwt
        return myJWT;

    }

    /**
     * 包含过期时间生成jwt
     * @param body
     * @param secretKey
     * @param expire
     * @return
     */
    public String generatorJWT(String body, SecretKey secretKey, long expire) {
        // 定义过期时间
        Date expireTime = new Date(System.currentTimeMillis() + expire);
        String myJWT = Jwts.builder()
                .setSubject(body)
                .signWith(secretKey)
                .compact();
        return myJWT;

    }

}
