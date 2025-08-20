package com.imooc.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.imooc.pojo.Users;

/**
* @author yangguang
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2025-08-20 12:15:56
*/
public interface UsersService {

    public Users getOne();
}
