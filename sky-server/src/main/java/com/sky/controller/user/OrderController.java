package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@RestController("userOrder")
@RequestMapping("/user/order")
@Api(tags = "用户订单接口")
public class OrderController {
    @Resource
    OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("分页获取用户历史订单")
    public Result<PageResult<OrderVO>> orderPageQuery(int page, int pageSize, Integer status){
        OrdersPageQueryDTO ordersPageQueryDTO = OrdersPageQueryDTO.builder()
                .page(page)
                .pageSize(pageSize)
                .status(status)
                .userId(BaseContext.getCurrentId()).build();
        log.info("分页查询历史订单:{}", ordersPageQueryDTO);
        PageResult<OrderVO> pageResult = orderService.orderPageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("根据订单id查询订单")
    public Result<OrderVO> orderQuery(@PathVariable long id){
        Orders orders = new Orders();
        orders.setId(id);
        log.info("根据订单id查询订单：{}",id);
        OrderVO orderVO = orderService.orderQuery(orders);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("根据id取消订单" )
    public Result<String> cancel(@PathVariable long id){
        log.info("取消订单：{}", id);
        Orders orders = new Orders();
        orders.setId(id);
        orderService.cancel(orders);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result<String> repetition(@PathVariable long id){
        log.info("再来一单：{}", id);
        Orders orders = new Orders();
        orders.setId(id);
        orderService.repetition(orders);
        return Result.success();
    }
}
