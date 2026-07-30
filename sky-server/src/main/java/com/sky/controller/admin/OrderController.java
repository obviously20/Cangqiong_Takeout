package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单管理")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation(value = "订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索参数：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 查询各种状态的订单数量统计
     * @return
     */
    @ApiOperation(value = "各种状态的订单数量统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        log.info("查询各种状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.getOrderStatistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @ApiOperation(value = "查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> getOrderVO(@PathVariable Long id) {
        log.info("查询订单详情，订单id：{}", id);
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @ApiOperation(value = "接单")
    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单，订单id：{}", ordersConfirmDTO.getId());
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }


    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation(value = "拒绝订单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("拒绝订单，订单id：{},拒绝原因：{}", ordersRejectionDTO.getId(), ordersRejectionDTO.getRejectionReason());
        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @ApiOperation(value = "取消订单")
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("取消订单，订单id：{},取消原因：{}", ordersCancelDTO.getId(), ordersCancelDTO.getCancelReason());
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }


    /**
     * 派送订单
     * @param id
     * @return
     */
    @ApiOperation(value = "派送订单")
    @PutMapping("/delivery/{id}")
    public Result deliveryOrder(@PathVariable Long id) {
        log.info("派送订单，订单id：{}", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }


    /**
     * 完成订单
     * @param id
     * @return
     */
    @ApiOperation(value = "完成订单")
    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id) {
        log.info("完成订单，订单id：{}", id);
        orderService.completeOrder(id);
        return Result.success();
    }

























}
