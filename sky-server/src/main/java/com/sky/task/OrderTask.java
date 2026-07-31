package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理待支付订单超时状态
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟执行一次任务
//    @Scheduled(cron = "0/5 * * * * ?")//test
    public void processOrder() {
        log.info("处理待支付订单超时状态");

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //select * from orders where status = ？ and order_time < 现在时间-15分钟
        List<Orders> orders = orderMapper.selectByStatusAndCreateTimeTL(Orders.PENDING_PAYMENT, time);

        if(orders != null && orders.size() > 0) {
            orders.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时未支付,自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            });
        }
    }

    /**
     * 处理一直派送中状态的订单，每天凌晨1点执行前一天的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点执行前一天的订单
//    @Scheduled(cron = "1/5 * * * * ?")//test
    public void processDeliverOrder() {
        log.info("处理一直派送中状态的订单");

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        //select * from orders where status = ？ and order_time < 现在时间-60分钟
        List<Orders> orders = orderMapper.selectByStatusAndCreateTimeTL(Orders.DELIVERY_IN_PROGRESS, time);

        if(orders != null && orders.size() > 0) {
            orders.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            });
        }


    }

}
