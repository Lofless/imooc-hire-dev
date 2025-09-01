package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.pojo.Admin;
import com.imooc.pojo.bo.createAdminBO;
import com.imooc.result.GraceJSONResult;
import com.imooc.service.AdminService;
import com.imooc.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("list")
    public GraceJSONResult list(String accountName, Integer page, Integer limit) {

        if(page == null) page = 1;
        if(limit == null) limit = 10;

        PagedGridResult listResult = adminService.list(accountName, page, limit);

        return GraceJSONResult.ok(listResult);
    }


}
