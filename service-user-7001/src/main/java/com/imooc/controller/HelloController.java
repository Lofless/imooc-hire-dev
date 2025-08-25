package com.imooc.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Stu;
import com.imooc.pojo.Users;
import com.imooc.service.StuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
@RequestMapping("u")
@Slf4j
public class HelloController extends BaseInfoProperties {

    private final StuService stuService;

    public HelloController(StuService stuService) {
        this.stuService = stuService;
    }

    @Value("${server.port}")
    private String port;

    @GetMapping("hello")
    public Object hello(HttpServletRequest request) {
        // 在网关中已经处理好了用户的信息，这边可以直接展示出来
        String userJson = request.getHeader(APP_USER_JSON);
        Users user = JSON.parseObject(userJson, Users.class);
        log.info("userJson:{}",user);
        log.info("port:{}",port);
        return "Hello UserService~~";
    }

    @GetMapping("stu")
    public Object stu(){
        Stu stu = new Stu();
        stu.setAge(18);
        stu.setName("imooc");
//        stu.setId(1010);

        stuService.save(stu);
        return "ok";
    }
}
