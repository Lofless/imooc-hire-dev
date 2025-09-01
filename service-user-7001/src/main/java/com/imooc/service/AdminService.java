package com.imooc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.pojo.Admin;
import com.imooc.pojo.bo.AdminBO;
import com.imooc.pojo.bo.createAdminBO;
import com.imooc.utils.PagedGridResult;

import javax.validation.Valid;
import java.util.List;

/**
* @author yangguang
* @description 针对表【admin(慕聘网运营管理系统的admin账户表，仅登录，不提供注册)】的数据库操作Service
* @createDate 2025-08-30 22:28:43
*/
public interface AdminService extends IService<Admin> {


    /**
     * 创建admin账号
     * @param createAdminBO
     */
    void createAdmin(createAdminBO createAdminBO);

    /**
     * 查询admin列表
     * @param accountName
     * @param page
     * @param limit
     * @return
     */
    PagedGridResult list(String accountName, Integer page, Integer limit);

    /**
     * 删除admin账号
     * @param username
     */
    void deleteAdmin(String username);
}
