package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {
    @Resource
    UserMapper userMapper;

    @Resource
    OrderMapper orderMapper;

    @Resource
    SetmealMapper setmealMapper;

    @Resource
    DishMapper dishMapper;

    @Override
    public BusinessDataVO businessData() {
        LocalDate now = LocalDate.now();
        LocalDateTime min = now.atStartOfDay();
        LocalDateTime max = now.plusDays(1).atStartOfDay();

        // 1.获取新增用户
        Map<String, Object> map = new HashMap<>();
        map.put("begin", min);
        map.put("end", max);
        map.put("start", min);
        Integer newUserNum = userMapper.getUserNumByTime(map);

        // 2.订单完成率
        // 订单总数 （status不加限制）

        Integer totalOrderCount = orderMapper.getOrderNumByTime(map);


        // 有效订单数（status = 5）
        Map<String,Object> map1 = new HashMap<>();
        map1.put("begin", min);
        map1.put("start", min);
        map1.put("end", max);
        map1.put("status", Orders.COMPLETED);
        Integer validOrderNum = orderMapper.getOrderNumByTime(map1);
        double orderCompleteRate =  totalOrderCount == 0 ? 0.0 :(double) validOrderNum/totalOrderCount;

        // 3. 营业额
        BigDecimal sumAmount = orderMapper.getSumAmount(map1);

        // 4. 平均客单价格
        List<BigDecimal> amountList = orderMapper.getmountWithUserId(map1);
        BigDecimal reduce = amountList.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal abs = reduce.divide(BigDecimal.valueOf(amountList.size()), 3, RoundingMode.HALF_UP);

        return BusinessDataVO.builder()
                .newUsers(newUserNum)
                .orderCompletionRate(orderCompleteRate)
                .turnover(sumAmount.doubleValue())
                .unitPrice(abs.doubleValue())
                .validOrderCount(validOrderNum).build();
    }

    @Override
    public SetmealOverViewVO setmealOverview() {
        Integer onSale = setmealMapper.getAmountByStatus(1);
        Integer offSale = setmealMapper.getAmountByStatus(0);
        return SetmealOverViewVO.builder().sold(onSale).discontinued(offSale).build();
    }

    @Override
    public DishOverViewVO dishOverview() {
        Integer onSale = dishMapper.getAmountByStatus(1);
        Integer offSale = dishMapper.getAmountByStatus(0);
        return DishOverViewVO.builder().sold(onSale).discontinued(offSale).build();
    }

    @Override
    public OrderOverViewVO orderOverview() {
        LocalDate now = LocalDate.now();
        LocalDateTime min = now.atStartOfDay();
        LocalDateTime max = now.plusDays(1).atStartOfDay();
        Map<String, Object> map = new HashMap<>();
        map.put("start", min);
        map.put("end", max);
        map.put("status", null);
        // 1.all
        map.replace("status", null);
        Integer all = orderMapper.getOrderNumByTime(map);
        // 2. cancel
        map.replace("status", Orders.CANCELLED);
        Integer cancel = orderMapper.getOrderNumByTime(map);
        // 3. completed
        map.replace("status", Orders.COMPLETED);
        Integer completed = orderMapper.getOrderNumByTime(map);
        // 4. delivery
        map.replace("status", Orders.CONFIRMED);
        Integer delivery = orderMapper.getOrderNumByTime(map);
        // 5. waiting
        map.replace("status", Orders.TO_BE_CONFIRMED);
        Integer waiting = orderMapper.getOrderNumByTime(map);

        return OrderOverViewVO.builder()
                .allOrders(all)
                .cancelledOrders(cancel)
                .completedOrders(completed)
                .deliveredOrders(delivery)
                .waitingOrders(waiting).build();

    }
}
