package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imooc.enums.Sex;
import com.imooc.enums.ShowWhichName;
import com.imooc.enums.UserRole;
import com.imooc.pojo.Users;
import com.imooc.service.UsersService;
import com.imooc.mapper.UsersMapper;
import com.imooc.utils.DesensitizationUtil;
import com.imooc.utils.LocalDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
* @author yangguang
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2025-08-20 12:15:56
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService{

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public Users getOne() {
        return usersMapper.selectById(1);
    }

    @Override
    public Users queryMobileIsExist(String mobile) {

        Users users = usersMapper.selectOne(new QueryWrapper<Users>()
                .eq("mobile", mobile));
        return users;
    }

    @Transactional
    @Override
    public Users createUsers(String mobile) {

        Users user = new Users();
        user.setMobile(mobile);
        user.setNickname("用户"+ DesensitizationUtil.commonDisplay(mobile));
        user.setRealName("用户"+ DesensitizationUtil.commonDisplay(mobile));
        user.setShowWhichName(ShowWhichName.nickname.type);

        user.setSex(Sex.secret.type);
        user.setFace("");
        user.setEmail("");

        // 使用LocalDate
        LocalDate birthday = LocalDateUtils.parseLocalDate(
                "1980-01-01",LocalDateUtils.DATE_PATTERN);
        user.setBirthday(birthday);
        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下～");

        // 我参加工作的日期默认使用注册当天的日期
        user.setStartWorkDate(LocalDate.now());
        user.setPosition("底层码农");
        user.setRole(UserRole.CANDIDATE.type);
        user.setHrInWhichCompanyId("");

        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        usersMapper.insert(user);

        return user;
    }

    @Override
    public Users getById(String userId) {
        Users user = usersMapper.selectById(userId);
        return user;
    }
}




