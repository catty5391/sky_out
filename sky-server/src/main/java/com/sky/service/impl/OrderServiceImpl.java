package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    AddressBookMapper addressBookMapper;

    @Resource
    ShoppingCartMapper shoppingCartMapper;

    @Resource
    OrderMapper orderMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    OrderDetailMapper orderDetailMapper;

    @Resource
    private WeChatPayUtil weChatPayUtil;

    @Resource
    private WebSocketServer webSocketServer;

    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);

        // 当前线程里有userId
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getUserById(userId);

        // 取出用户使用地址
        AddressBook addressbook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        // 取出用户购物车内商品
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);

        // 处理错误
        if(addressbook == null) throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        if(shoppingCarts == null || shoppingCarts.isEmpty()) throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        // 生成订单编号
        String orderNumber = String.valueOf(System.currentTimeMillis());

        // 生成订单
        order.setUserName(user.getName());
        order.setNumber(orderNumber);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setUserId(userId);
        order.setAddressBookId(addressbook.getId());
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        order.setPhone(addressbook.getPhone());
        order.setAddress(addressbook.getDetail());
        order.setConsignee(addressbook.getConsignee());

        orderMapper.add(order);


        // 将购物车内容添加到订单明细表
        List<OrderDetail> orderDetailList = new ArrayList<>();
        shoppingCarts.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetailList);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(ordersSubmitDTO.getAmount()).build();

        // 清空购物车
        shoppingCartMapper.delete(shoppingCart);

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getUserById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        JSONObject jsonObject = new JSONObject();

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过websocket 向管理端推送消息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 1);
        jsonObject.put("content", "订单号：" + outTradeNo);
        jsonObject.put("orderId", ordersDB.getId());
        webSocketServer.sendToAllClient(jsonObject.toString());
    }

    /**
     * 用户端分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult<OrderVO> orderPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<OrderVO> res_list = new ArrayList<>();
        try (Page<Orders> page = orderMapper.page(ordersPageQueryDTO)) {
            List<Orders> list = page.getResult();
            long total = page.getTotal();
            list.forEach( od ->{
                OrderVO orders = new OrderVO();
                BeanUtils.copyProperties(od, orders);
                orders.setOrderDetailList(orderDetailMapper.getByOrderId(od.getId()));
                res_list.add(orders);
            });

            PageResult<OrderVO> pageResult = new PageResult<>(total, res_list);
            return pageResult;
        }
    }

    @Override
    public OrderVO orderQuery(Orders orders) {
         Orders res_order= orderMapper.getById(orders.getId());
         OrderVO orderVO = new OrderVO();
         BeanUtils.copyProperties(res_order, orderVO);
         orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(res_order.getId()));
         return orderVO;
    }

    @Override
    public void cancel(Orders orders) {
        // 查询orders
        Orders resOrder = orderMapper.getById(orders.getId());
        if(resOrder == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(resOrder.getStatus() > Orders.TO_BE_CONFIRMED) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);


        if(resOrder.getPayStatus().equals(Orders.PAID)) {
            orders.setPayStatus(Orders.REFUND);
        }

        // 补充订单取消相关信息
        orders.setCancelReason("用户取消订单");
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    @Override
    public void repetition(Orders orders) {
        // 将订单id的菜品添加到购物车即可
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(od ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(od, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public OrderStatisticsVO orderNumCount() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        orderStatisticsVO.setToBeConfirmed(orderMapper.countByStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.countByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS));
        return  orderStatisticsVO;
    }

    /**
     * 管理端根据条件分页查询订单信息
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult<OrderVO> conditionQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> orders = orderMapper.page(ordersPageQueryDTO);
        List<Orders> ordersList = orders.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();

        ordersList.forEach(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
            orderVO.setOrderDetailList(orderDetailList);
            orderVO.setOrderDishes(getOrderDishesStr(orderDetailList));
            orderVOList.add(orderVO);
        });

        return new PageResult<>(orders.getTotal(), orderVOList);
    }

    /**
     * 接单
     * @param orders
     */
    @Override
    public void confirm(Orders orders) {
        // 校验是否属于待接单
        Orders order = orderMapper.getById(orders.getId());
        if(!order.getStatus().equals(Orders.TO_BE_CONFIRMED)) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        order.setStatus(Orders.CONFIRMED);
        orderMapper.update(order);
    }

    @Override
    public void rejection(Orders orders) {
        // 只有处于待接单状态的订单才可以拒单
        Orders order = orderMapper.getById(orders.getId());
        if(order == null || !order.getStatus().equals(Orders.TO_BE_CONFIRMED)) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        // 将支付状态改为退款，将订单状态改为取消
        if(order.getPayStatus().equals(Orders.PAID)) order.setPayStatus(Orders.REFUND);

        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(orders.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void adminCancel(Orders orders) {
        // 查询orders
        Orders resOrder = orderMapper.getById(orders.getId());
        if(resOrder == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        if(resOrder.getStatus().equals(Orders.TO_BE_CONFIRMED) || resOrder.getStatus().equals(Orders.COMPLETED)) throw new OrderBusinessException(MessageConstant.ORDER_CANCEL_ERROR);


        if(resOrder.getPayStatus().equals(Orders.PAID)) {
            orders.setPayStatus(Orders.REFUND);
        }

        // 补充订单取消相关信息
        orders.setCancelReason(orders.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Orders orders) {
        Orders resOrder = orderMapper.getById(orders.getId());
        if(!resOrder.getStatus().equals(Orders.CONFIRMED)) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        Orders order = new Orders();
        order.setId(orders.getId());
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    @Override
    public void complete(Orders orders) {
        Orders resOrder = orderMapper.getById(orders.getId());
        if(!resOrder.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        Orders order = new Orders();
        order.setId(orders.getId());
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void remind(long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber() );
        String res = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(res);
    }


    private String getOrderDishesStr(List<OrderDetail> orderDetailList) {
        List<String> strings = orderDetailList.stream().map(orderDetail -> {
            return orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
        }).collect(Collectors.toList());

        return String.join(";", strings);
    }


}
