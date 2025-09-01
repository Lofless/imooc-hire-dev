package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.exceptions.GraceException;
import com.imooc.mapper.AdminMapper;
import com.imooc.pojo.Admin;
import com.imooc.pojo.bo.AdminBO;
import com.imooc.pojo.bo.createAdminBO;
import com.imooc.result.ResponseStatusEnum;
import com.imooc.service.AdminService;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author yangguang
* @description 针对表【admin(慕聘网运营管理系统的admin账户表，仅登录，不提供注册)】的数据库操作Service实现
* @createDate 2025-08-30 22:28:43
*/
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private BaseInfoProperties baseInfoProperties;

    @Transactional
    @Override
    public void createAdmin(createAdminBO createAdminBO) {

        // 查看这个账号是否存在，如果存在就不允许注册
        Admin admin = getSelfAdmin(createAdminBO.getUsername());
        // 优雅异常解耦完美体验
        if(admin != null) GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);

        Admin newAdmin = new Admin();
        BeanUtils.copyProperties(createAdminBO, newAdmin);
        String slat = (int)((Math.random() * 9 + 1) * 100000) + "";
        String pwd = MD5Utils.encrypt(createAdminBO.getPassword(), slat);
        newAdmin.setPassword(pwd);
        newAdmin.setSlat(slat);

        newAdmin.setCreateTime(LocalDateTime.now());
        newAdmin.setUpdatedTime(LocalDateTime.now());

        adminMapper.insert(newAdmin);
    }

    @Override
    public PagedGridResult list(String accountName, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        List<Admin> adminList = adminMapper.selectList(
                new QueryWrapper<Admin>()
                        .like("username", accountName)
        );
        log.info("adminListSize:{}", adminList.size());
        return baseInfoProperties.setterPagedGrid(adminList, page);
    }

    public Admin getSelfAdmin(String username) {
        return adminMapper.selectOne(
                new QueryWrapper<Admin>()
                        .eq("username", username)
        );
    }
}




