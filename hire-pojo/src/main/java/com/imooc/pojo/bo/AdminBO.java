package com.imooc.pojo.bo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class AdminBO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "验证码不能为空")
    private String password;

}
