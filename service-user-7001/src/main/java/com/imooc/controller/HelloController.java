package com.imooc.controller;

import com.imooc.pojo.Stu;
import com.imooc.service.StuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("u")
@Slf4j
public class HelloController {

    private final StuService stuService;

    public HelloController(StuService stuService) {
        this.stuService = stuService;
    }

    @Value("${server.port}")
    private String port;

    @GetMapping("hello")
    public Object hello(){
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
