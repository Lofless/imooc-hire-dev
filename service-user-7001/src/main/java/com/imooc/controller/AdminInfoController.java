package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.bo.createAdminBO;
import com.imooc.result.GraceJSONResult;
import com.imooc.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admininfo")
@Slf4j
public class AdminInfoController extends BaseInfoProperties {

    @Autowired
    private AdminService adminService;

    @PostMapping("create")
    public GraceJSONResult createAdmin(@Validated @RequestBody createAdminBO createAdminBO) {
        adminService.createAdmin(createAdminBO);
        return GraceJSONResult.ok();
    }


}
