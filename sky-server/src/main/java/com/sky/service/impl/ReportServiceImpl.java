package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.Sales;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    OrderMapper orderMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    OrderDetailMapper orderDetailMapper;
    /**
     * 返回指定时间内的营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        // 1.日期列表
        // 方法1，while循环
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        // 方法2，stream流, 需要先计算有几天
        long between = ChronoUnit.DAYS.between(begin, end) + 1;
        List<LocalDate> datelist2 = Stream.iterate(begin, date ->
                date.plusDays(1)
        ).limit(between).collect(Collectors.toList());


        // 2.营业额列表
        List<BigDecimal> turnoverList = new ArrayList<>();
        for(LocalDate date: dateList){
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = startTime.plusDays(1);
            Map<String, Object> map = new HashMap<>();
            map.put("start", startTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            BigDecimal decimal = orderMapper.getSumAmount(map);
            decimal = decimal == null ? BigDecimal.ZERO : decimal;
            turnoverList.add(decimal);
        }
        // List<String> stringList = dateList.stream().map(LocalDate::toString).collect(Collectors.toList());
        // turnoverReportVO.setDateList(String.join(",", stringList));

        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 统计在指定日期的新增用户， 统计到指定日期为止总用户个数

        // 1.统计所有日期
        List<LocalDate> localDates = new ArrayList<>();
        while(!begin.isAfter(end)){
            localDates.add(begin);
            begin = begin.plusDays(1);
        }
        String dateList = StringUtils.join(localDates, ",");
//
//        // 2.统计新增用户
//        List<Integer> newUsers = new ArrayList<>();
//        for (LocalDate localDate : localDates) {
//            LocalDateTime beginTime = localDate.atStartOfDay();
//            LocalDateTime endTime = localDate.plusDays(1).atStartOfDay();
//            Map<String, Object> map = new HashMap<>();
//            map.put("begin", beginTime);
//            map.put("end", endTime);
//            Integer newUser = userMapper.getUserNumByTime(map);
//            newUsers.add(newUser);
//        }
//        String newUserList = StringUtils.join(newUsers, ",");
//
//        // 3. 统计所有用户
//        List<Integer> allUsers = new ArrayList<>();
//        for (LocalDate localDate : localDates) {
//            LocalDateTime endTime = localDate.plusDays(1).atStartOfDay();
//            Map<String, Object> map = new HashMap<>();
//            map.put("end", endTime);
//            Integer newUser = userMapper.getUserNumByTime(map);
//            allUsers.add(newUser);
//        }
//        String totalUserList = StringUtils.join(allUsers, ",");


        // 2.3 合并
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> allUsers = new ArrayList<>();
        for (LocalDate localDate : localDates) {
            LocalDateTime beginTime = localDate.atStartOfDay();
            LocalDateTime endTime = localDate.plusDays(1).atStartOfDay();
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer newUser = userMapper.getUserNumByTime(map);
            newUsers.add(newUser);

            Map<String, Object> map1 = new HashMap<>();
            map1.put("end", endTime);
            Integer allUser = userMapper.getUserNumByTime(map1);
            allUsers.add(allUser);
        }
        String newUserList = StringUtils.join(newUsers, ",");
        String totalUserList = StringUtils.join(allUsers, ",");


        UserReportVO reportVO = UserReportVO.builder()
                .dateList(dateList)
                .newUserList(newUserList)
                .totalUserList(totalUserList).build();

        return reportVO;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 1.统计所有日期
        List<LocalDate> localDates = new ArrayList<>();
        while(!begin.isAfter(end)){
            localDates.add(begin);
            begin = begin.plusDays(1);
        }
        String dateList = StringUtils.join(localDates, ",");

        // 2.循环查询
        List<Integer> orderCounts = new ArrayList<>();
        List<Integer> validOrderCounts = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validTotalOrderCount = 0;

        for (LocalDate date : localDates){
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            // 订单总数 （status不加限制）
            Map<String, Object> map = new HashMap<>();
            map.put("start", beginTime);
            map.put("end", endTime);
            Integer orderNum = orderMapper.getOrderNumByTime(map);
            totalOrderCount += orderNum;
            orderCounts.add(orderNum);

            // 有效订单数（status = 5）
            map.put("status", Orders.COMPLETED);
            Integer validOrderNum = orderMapper.getOrderNumByTime(map);
            validTotalOrderCount += validOrderNum;
            validOrderCounts.add(validOrderNum);
        }

        return OrderReportVO.builder()
                .dateList(dateList)
                .orderCompletionRate(totalOrderCount == 0 ? 0.0 :(double) validTotalOrderCount/totalOrderCount)
                .orderCountList(StringUtils.join(orderCounts, ","))
                .validOrderCountList(StringUtils.join(validOrderCounts, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validTotalOrderCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10Statistics(LocalDate begin, LocalDate end) {
        // 1.统计所有日期
        List<LocalDate> localDates = new ArrayList<>();
        while(!begin.isAfter(end)){
            localDates.add(begin);
            begin = begin.plusDays(1);
        }

        //
        List<Sales> salesTop10List = new ArrayList<>();
        for(LocalDate date: localDates){
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            salesTop10List = orderDetailMapper.top10(map);
        }
        List<String> names = salesTop10List.stream().map(Sales::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10List.stream().map(Sales::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .numberList(StringUtils.join(numbers, ","))
                .nameList(StringUtils.join(names, ","))
                .build();

    }
}
