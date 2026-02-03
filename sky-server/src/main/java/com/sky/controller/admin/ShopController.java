package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api("商店相关接口")
public class ShopController {

    @Resource
    RedisTemplate redisTemplate;

    /**
     * 设置商店状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置商店状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置商店状态为：{}", status == 1 ? "营业" : "打样");
        redisTemplate.opsForValue().set("SHOP_STATUS",status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("店铺状态为：{}", status == 1 ? "营业" : "打烊");
        return Result.success(status);
    }


}
