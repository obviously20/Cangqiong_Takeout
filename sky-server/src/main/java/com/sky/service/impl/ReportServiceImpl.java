package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceServiceImpl workspaceServiceImpl;

    /**
     * 营业额统计
     *
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
     *
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
     *
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
        if (totalOrderCount != 0) {
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
     * 销售Top10统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {
        //开始和结束时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //查询商品Top10
        List<GoodsSalesDTO> goodsSalesDTOList = orderDetailMapper.getTop10ByMap(beginTime, endTime);
        //将商品名称和销量转换为字符串
        List<String> nameList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出excel报表
     *
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        //在数据库中查询数据
        LocalDate now = LocalDate.now();//获取当前日期
        LocalDate beginDate = now.minusDays(30);//获取30天前的日期(开始日期)
        LocalDate endDate = now.minusDays(1);//获取当前日期(结束日期)

        //查询30天营业额统计
        BusinessDataVO businessDataVO = workspaceServiceImpl.getBusinessData(
                LocalDateTime.of(beginDate, LocalTime.MIN),
                LocalDateTime.of(endDate, LocalTime.MAX));

        //将查询结果以模板写入(//将数据库查到的数据以template包中的excel为模板写入)
        //先获取模板文件的输入流
        // this.getClass().getClassLoader() 获取当前类的类加载器
        // getResourceAsStream("template/运营数据报表模板.xlsx") 获取模板文件的输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //创建Excel文件对象
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            // 获取指定的sheet表
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //获取指定的行
            XSSFRow row = sheet.getRow(1);

            // 写该表的时间范围
            // (1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK) 表示如果该单元格为空，就创建一个空的单元格
            row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue("时间范围：" + beginDate + "至" + endDate);

            // 写总的数据
            row = sheet.getRow(3);
            row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(businessDataVO.getTurnover());
            row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(businessDataVO.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(businessDataVO.getUnitPrice());

            // 现在写每天具体的信息
            for (int i = 0; i < 30; i++) {
                // 日期
                LocalDate date = beginDate.plusDays(i);
                // 从数据库查询该日期的营业额统计
                BusinessDataVO bd = workspaceServiceImpl.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                // 写该日期的营业额统计
                row = sheet.getRow(i + 7);
                row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(date.toString());
                row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(bd.getTurnover());
                row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(bd.getValidOrderCount());
                row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(bd.getOrderCompletionRate());
                row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(bd.getUnitPrice());
                row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).setCellValue(bd.getNewUsers());

            }


            //将完善好的execl表，让浏览器下载
            ServletOutputStream output = response.getOutputStream();
            excel.write(output);

            //关闭资源
            excel.close();
            output.flush();
            output.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 根据时间区间统计指定状态的订单数量
     *
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