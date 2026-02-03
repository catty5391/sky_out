package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api("商店相关接口")
public class ShopController {

    @Resource
    RedisTemplate redisTemplate;


    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("店铺状态为：{}", status == 1 ? "营业" : "打烊");
        return Result.success(status);
    }


}
