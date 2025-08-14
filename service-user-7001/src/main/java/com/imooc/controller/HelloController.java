package com.imooc.controller;

import com.imooc.pojo.Stu;
import com.imooc.service.StuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("u")
public class HelloController {

    private final StuService stuService;

    public HelloController(StuService stuService) {
        this.stuService = stuService;
    }

    @GetMapping("hello")
    public Object hello(){
        return "Hello UserService~~";
    }

    @GetMapping("stu")
    public Object stu(){
        Stu stu = new Stu();
        stu.setAge(18);
        stu.setName("imooc");
        stu.setId(1010);

        stuService.save(stu);
        return "ok";
    }
}
