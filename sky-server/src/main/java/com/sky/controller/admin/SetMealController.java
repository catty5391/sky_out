package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("adminSetMealController")
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
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

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult<SetmealVO>> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult<SetmealVO> pageResult = setMealService.page(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result<String> deletePatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐，套餐id为：{}", ids);
        setMealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id返回套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("查询套餐，id为：{}", id);
        SetmealVO setmealVO = setMealService.getById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result<String> update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息为：{}", setmealDTO);
        setMealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("更改套餐状态")
    public Result<String> status(@PathVariable Integer status, Long id){
        log.info("更改套餐{}的状态为{}", id, status == 1 ? "起售" : "停售");
        setMealService.updateStatus(status, id);
        return Result.success();
    }
}
