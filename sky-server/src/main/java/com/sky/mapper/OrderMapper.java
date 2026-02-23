package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入新订单
     * @param order
     */
    void add(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(long id);

    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    @Update("update orders set status = 6, cancel_time = #{cancelTime}, cancel_reason = #{cancelReason} where order_time <= #{orderTime} and status = #{status}")
    void outTimeUpdate(Orders orders);

    @Update("update orders set status = 5 where status = #{status}")
    void deliveryUpdate(Orders orders);

    BigDecimal getSumAmount(Map<String, Object> map);

    Integer getOrderNumByTime(Map<String, Object> map);

    @Select("select sum(amount) from orders where status = #{status} and order_time > #{begin} and order_time < #{end} group by user_id")
    List<BigDecimal> getmountWithUserId(Map<String, Object> map);
}
