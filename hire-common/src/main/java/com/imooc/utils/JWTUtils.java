package com.imooc.utils;

import com.imooc.exceptions.GraceException;
import com.imooc.result.ResponseStatusEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * 四种组合方式生成JWT_TOKEN
 * 1. 是否带有前缀
 * 2. 是否带有过期时间
 */
@Component
@Slf4j
public class JWTUtils {

    @Autowired
    private JWTProperties jwtProperties;

    public static final String at = "@";

    /**
     * 有前缀
     * @param body
     * @param prefix
     * @return
     */
    public String createJWTWithPrefix(String body, String prefix){
        return prefix + at +  dealJWT(body, null);
    }

    /**
     * 有前缀，有过期时间
     * @param body
     * @param expire
     * @param prefix
     * @return
     */
    public String createJWTWithPrefix(String body, Long expire, String prefix){
        if (expire == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_NO_EXPIRE_ERROR);
        }
        return prefix + at + dealJWT(body, expire);
    }

    /**
     * 无前缀，无过期时间
     * @param body
     * @return
     */
    public String createJWT(String body){
        return dealJWT(body, null);
    }

    /**
     * 无前缀，有过期时间
     * @param body
     * @param expire
     * @return
     */
    public String createJWT(String body, Long expire){
        if (expire == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_NO_EXPIRE_ERROR);
        }
        return dealJWT(body, expire);
    }

    /**
     * 前置操作+生成token
     * @param body
     * @param expire
     * @return
     */
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
     * 无过期时间生成token
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
     * 有过期时间生成token
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
                .setExpiration(expireTime)
                .compact();
        return myJWT;

    }

    /**
     * 解析jwt获得对象
     * @param pendingJWT
     * @return
     */
    public String checkJWT(String pendingJWT) {

        String userKey = jwtProperties.getKey();
        // 对密钥进行base64编码
        String base64 = new BASE64Encoder().encode(userKey.getBytes());
        // 对base64生成一个密钥对象
        SecretKey secretKey = Keys.hmacShaKeyFor(base64.getBytes());

        // 校验jwt
        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();   //  构建解析器
        // 解析成功吗可以获得Clams
        Jws<Claims> jws = jwtParser.parseClaimsJws(pendingJWT);
        String body = jws.getBody().getSubject();
        return body;
    }

}
