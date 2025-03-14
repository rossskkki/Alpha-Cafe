package com.siki.mapper;

import com.siki.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单详情
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 根据订单号查询订单详情
     * @param number
     * @return
     */
    @Select("select * from order_detail left join orders on order_detail.order_id = orders.id where orders.number = #{number}")
    List<OrderDetail> getByOrderNumber(String number);
}
