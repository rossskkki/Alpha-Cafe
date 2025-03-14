package com.siki.controller.admin;

import com.siki.dto.OrdersCancelDTO;
import com.siki.dto.OrdersConfirmDTO;
import com.siki.dto.OrdersPageQueryDTO;
import com.siki.dto.OrdersRejectionDTO;
import com.siki.result.PageResult;
import com.siki.result.Result;
import com.siki.service.OrderService;
import com.siki.vo.OrderStatisticsVO;
import com.siki.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "后台管理系统订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 分页查询订单
     * @param orderPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation(value = "分页查询订单")
    public Result<PageResult> page (OrdersPageQueryDTO orderPageQueryDTO){
        log.info("分页查询订单：{}", orderPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(orderPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 订单统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "订单统计")
    public Result<OrderStatisticsVO> statistics(){
        log.info("订单统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation(value = "订单详情")
    public Result<OrderVO> detail(@PathVariable Long id){
        log.info("订单详情：{}", id);
        OrderVO orderVO = orderService.detail(id);
        return Result.success(orderVO);
    }

    /**
     * 确认订单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation(value = "确认订单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("确认订单：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation(value = "拒绝订单")
    public Result rejuct(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒绝订单：{}", ordersRejectionDTO);
        orderService.rejuct(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation(value = "取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.cancelOrderByAdmin(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 订单发货
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation(value = "订单发货")
    public Result delivery(@PathVariable Long id){
        log.info("订单发货：{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation(value = "完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单：{}", id);
        orderService.complete(id);
        return Result.success();
    }
}
