package com.siki.service;

import com.siki.dto.*;
import com.siki.result.PageResult;
import com.siki.result.Result;
import com.siki.vo.OrderPaymentVO;
import com.siki.vo.OrderStatisticsVO;
import com.siki.vo.OrderSubmitVO;
import com.siki.vo.OrderVO;

public interface OrderService {

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 分页查询订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQuery(int page, int pageSize, Integer status);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 取消订单
     * @param id
     */
    void cancelOrder(Long id);

    /**
     * 重复下单
     * @param id
     */
    void repetOrder(Long id);

    /**
     * 条件查询订单
     * @param orderPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO orderPageQueryDTO);

    /**
     * 订单统计
     * @return
     */
    OrderStatisticsVO statistics();

    OrderVO detail(Long id);

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejuct(OrdersRejectionDTO ordersRejectionDTO);

    void cancelOrderByAdmin(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);

    void orderRemind(Long id);
}
