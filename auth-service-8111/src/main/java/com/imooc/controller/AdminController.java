package com.imooc.controller;

import com.alibaba.fastjson.JSONObject;
import com.imooc.base.BaseInfoProperties;
import com.imooc.intercept.JWTCurrentUserInterceptor;
import com.imooc.pojo.Admin;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.AdminBO;
import com.imooc.pojo.vo.AdminVO;
import com.imooc.pojo.vo.SaasUserVO;
import com.imooc.result.GraceJSONResult;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.service.AdminService;
import com.imooc.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController extends BaseInfoProperties {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody AdminBO adminBO) {

        boolean isExist = adminService.adminLogin(adminBO);
        if(!isExist) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_LOGIN_ERROR);
        }

        // 登录成功之后获得admin信息
        Admin admin = adminService.getAdminInfo(adminBO);
        String adminToken = jwtUtils.createJWTWithPrefix(JSONObject.toJSON(admin).toString(), TOKEN_ADMIN_PREFIX);

        return GraceJSONResult.ok(adminToken);
    }

    @GetMapping("info")
    public GraceJSONResult info(String token) {

        Admin admin = JWTCurrentUserInterceptor.adminUser.get();
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);

        return GraceJSONResult.ok(adminVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout() {

        return GraceJSONResult.ok();
    }

}
