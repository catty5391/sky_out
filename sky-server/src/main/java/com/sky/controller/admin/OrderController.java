package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.Resource;

@RestController("adminOrder")
@Slf4j
@Api(tags = "管理端订单接口")
@RequestMapping("/admin/order")
public class OrderController {

    @Resource
    OrderService orderService;
    @Autowired
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @GetMapping("/conditionSearch")
    @ApiOperation("条件搜索")
    public Result<PageResult<OrderVO>> list(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult<OrderVO> pageResult = orderService.conditionQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("统计不同status订单数量")
    public Result<OrderStatisticsVO> orderNumCount(){
        OrderStatisticsVO orderStatisticsVO = orderService.orderNumCount();
        return Result.success(orderStatisticsVO);
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> getById(@PathVariable long id){
        Orders orders = new Orders();
        orders.setId(id);

        OrderVO orderVO = orderService.orderQuery(orders);
        return Result.success(orderVO);
    }

    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result<String> confirm(@RequestBody Orders orders){
        orderService.confirm(orders);
        return Result.success();
    }

    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result<String> rejection(@RequestBody Orders orders){
        orderService.rejection(orders);
        return Result.success();
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result<String> cancel(@RequestBody Orders orders){
        orderService.adminCancel(orders);
        return Result.success();
    }

    @ApiOperation("派送")
    @PutMapping("/delivery/{id}")
    public Result<String> delivery(@PathVariable long id){
        Orders orders = new Orders();
        orders.setId(id);
        orderService.delivery(orders);
        return Result.success();
    }

    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result<String> complete(@PathVariable long id){
        Orders orders = new Orders();
        orders.setId(id);
        orderService.complete(orders);
        return Result.success();
    }
}
