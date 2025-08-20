package com.imooc.service.impl;

import com.imooc.pojo.Users;
import com.imooc.service.UsersService;
import com.imooc.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author yangguang
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2025-08-20 12:15:56
*/
@Service
public class UsersServiceImpl implements UsersService{

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public Users getOne() {
        return usersMapper.selectById(1);
    }
}




