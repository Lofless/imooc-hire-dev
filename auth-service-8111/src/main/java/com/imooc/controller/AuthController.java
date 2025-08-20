package com.imooc.controller;

import com.imooc.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("a")
@Slf4j
public class AuthController {

    @Autowired
    private UsersService usersService;

    @GetMapping("hello")
    public Object stu(){
        return "Hello Service!~!";
    }

    @GetMapping("test")
    public Object test(){
        return usersService.getOne();
    }
}
