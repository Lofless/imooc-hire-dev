package com.imooc.service.impl;

import com.imooc.utils.MD5Utils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.pojo.Admin;
import com.imooc.pojo.bo.AdminBO;
import com.imooc.service.AdminService;
import com.imooc.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author yangguang
* @description 针对表【admin(慕聘网运营管理系统的admin账户表，仅登录，不提供注册)】的数据库操作Service实现
* @createDate 2025-08-30 22:28:43
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public boolean adminLogin(AdminBO adminBO) {

        // 根据用户名获得slat
        Admin admin = getSelfAdmin(adminBO.getUsername());

        // 判断admin是否为空来返回true或false
        if(admin == null) {
            return false;
        }else {
            String slat = admin.getSlat();
            String md5Str = MD5Utils.encrypt(adminBO.getPassword(), slat);
            return md5Str.equals(admin.getPassword());
        }

    }

    @Override
    public Admin getAdminInfo(AdminBO adminBO) {
        return getSelfAdmin(adminBO.getUsername());
    }

    @Override
    public Admin getSelfAdmin(String username) {
        return adminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .eq("username", username)
        );
    }
}




