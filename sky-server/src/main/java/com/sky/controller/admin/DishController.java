package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "菜品相关操作")
@RequestMapping("/admin/dish")
public class DishController {

    @Resource
    DishService dishService;

    @ApiOperation("新增菜品")
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.save(dishDTO);

        return Result.success("success");
    }

    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult<DishVO>> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult<DishVO> pageResult = dishService.page(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    @ApiOperation("菜品批量删除")
    @DeleteMapping
    public Result<String> deleteBatch(@RequestParam List<Long> ids){
        log.info("删除菜品id: {}", ids);
        dishService.deleteBatch(ids);

        return Result.success("success");
    }

    @ApiOperation("起售或停售菜品")
    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable("status") int status, long id){
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishService.updateStatus(dish);
        return Result.success("success");
    }

    @ApiOperation("根据id返回菜品")
    @GetMapping("/{id}")
    public Result<DishVO> findById(@PathVariable Long id){
        log.info("根据菜品id查询菜品{}", id);
        DishVO dishVO= dishService.findById(id);
        return Result.success(dishVO);
    }

    @ApiOperation("修改菜品")
    @PutMapping
    public Result<String> updateDish(@RequestBody DishDTO dishDTO){

        dishService.update(dishDTO);

        return Result.success("success");
    }



}
