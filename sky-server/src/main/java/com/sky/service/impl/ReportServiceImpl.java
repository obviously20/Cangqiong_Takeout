package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

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

}