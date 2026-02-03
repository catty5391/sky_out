package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("adminSetMealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api("套餐相关接口")
public class SetMealController {

    @Resource
    SetMealService setMealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result addSetMeal(@RequestBody SetmealDTO setmealDTO){
        log.info("添加新套餐：{}", setmealDTO);
        setMealService.addSetMeal(setmealDTO);

        return Result.success();
    }
}
