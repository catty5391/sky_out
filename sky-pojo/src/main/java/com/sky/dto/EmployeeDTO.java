package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户注册数据")
public class EmployeeDTO implements Serializable {


    private Long id;
    
    @ApiModelProperty("账号（唯一）")
    private String username;

    private String name;

    private String phone;

    private String sex;

    @ApiModelProperty("身份证号")
    private String idNumber;

}
