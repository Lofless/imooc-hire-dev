package com.imooc.pojo.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class createAdminBO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "验证码不能为空")
    private String password;

    private String remark;

}
