package com.sky.controller.admin;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Api(tags = "工作台接口")
@RequestMapping("/admin/workspace")
@RestController
public class WorkSpaceController {


    @Resource
    WorkSpaceService workSpaceService;

    @ApiOperation("获取当日运营数据")
    @GetMapping("/businessData")
    public Result<BusinessDataVO> business(){

        BusinessDataVO businessDataVO = workSpaceService.businessData();
        return Result.success(businessDataVO);

    }

    @ApiOperation("获取套餐相关数据")
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> setmealOverView(){
        SetmealOverViewVO setmealOverViewVO = workSpaceService.setmealOverview();
        return Result.success(setmealOverViewVO);
    }

    @ApiOperation("获取套餐相关数据")
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> dishOverview(){
        DishOverViewVO dishOverViewVO = workSpaceService.dishOverview();
        return Result.success(dishOverViewVO);
    }

    @ApiOperation("订单相关查询")
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> orderOverView(){
        OrderOverViewVO orderOverViewVO = workSpaceService.orderOverview();
        return Result.success(orderOverViewVO);

    }

}
