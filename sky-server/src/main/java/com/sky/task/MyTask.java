package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class MyTask {

    @Resource
    OrderMapper orderMapper;

    //@Scheduled(cron = "0/5 * * * * ?")
    public void test(){
        Integer n = 1;
        log.info("定时任务执行第{}次，时间为{}", n, new Date());
        n = n + 1;
    }

    // 处理未支付订单, 每分钟检查
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        // 对于超过15分钟未支付的订单直接取消(支付状态为0，且下单时间超过15分钟)
        log.info("定时处理超时订单 {}", LocalDateTime.now());
        // payStatus = 1, 将当前时间向前推15分钟作为endtime
        LocalDateTime endTime = LocalDateTime.now().minusMinutes(15);
        Orders orders = Orders.builder()
                .orderTime(endTime)
                .status(Orders.PENDING_PAYMENT)
                .cancelReason(MessageConstant.ORDER_TIME_OUT)
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.outTimeUpdate(orders);
    }

    // 处理派送中订单，每天凌晨一点检查
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        Orders orders = Orders.builder()
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.deliveryUpdate(orders);
    }
}
