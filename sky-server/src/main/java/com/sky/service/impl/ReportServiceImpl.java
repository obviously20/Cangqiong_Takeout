package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        //先返回开始日期到结束日期的日期列表
        dateList.add(begin);//先将开始日期添加到列表中

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//将日期日期增加1天
            dateList.add(begin);//将增加后的日期添加到列表中
        }

        List<Double> turnoverList = new ArrayList<>();
        //再返回这个日期列表对应的营业额列表
        for (LocalDate date : dateList) {
            LocalDateTime beginDate = LocalDateTime.of(date, LocalTime.MIN);//将日期转换为开始时间 000:00:00
            LocalDateTime endDate = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("beginDate", beginDate);
            map.put("endDate", endDate);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getTurnoverByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        //先返回开始日期到结束日期的日期列表
        dateList.add(begin);//先将开始日期添加到列表中

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//将日期日期增加1天
            dateList.add(begin);//将增加后的日期添加到列表中
        }

        //新增用户列表
        List<Integer> newUserList = new ArrayList<>();
        //用户总量列表
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("endTime", endTime);

            //先查寻截至今日总的用户select count(id) from orders where order_time <= #{endTime}
            Integer totalUser = userMapper.userSumByMap(map);

            //再查寻截至今日新增的用户select count(id) from orders where order_time >= #{beginTime} and order_time <= #{endTime}
            map.put("beginTime", beginTime);
            Integer newUser = userMapper.userSumByMap(map);

            //将查询结果添加到列表中
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        //先返回开始日期到结束日期的日期列表
        dateList.add(begin);//先将开始日期添加到列表中

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//将日期日期增加1天
            dateList.add(begin);//将增加后的日期添加到列表中
        }

        //有效订单数列表
        List<Integer> validOrderCountList = new ArrayList<>();

        //订单总数列表
        List<Integer> totalOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询订单总数
            Integer totalOrderCount = getOrderCountByMap(beginTime, endTime, null);
            //查询有效订单数
            Integer validOrderCount = getOrderCountByMap(beginTime, endTime, Orders.COMPLETED);

            //将查询结果添加到列表中
            validOrderCountList.add(validOrderCount);
            totalOrderCountList.add(totalOrderCount);

        }

        //订单总数
        Integer totalOrderCount = totalOrderCountList.stream().reduce(Integer::sum).get();

        //有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    /**
     * 根据时间区间统计指定状态的订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCountByMap(LocalDateTime begin, LocalDateTime end, Integer status) {

        Map map = new HashMap();
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("status", status);

        return orderMapper.orderSumByMap(map);
    }


}