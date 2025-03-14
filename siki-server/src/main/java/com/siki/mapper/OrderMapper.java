package com.siki.mapper;

import com.github.pagehelper.Page;
import com.siki.dto.GoodsSalesDTO;
import com.siki.dto.OrdersPageQueryDTO;
import com.siki.entity.Orders;
import com.siki.vo.OrderVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 修改订单状态
     * @param id
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 取消订单
     * @param id
     */
    @Update("update orders set status = 6 where id = #{id}")
    void cancel(Long id);

    /**
     * 管理端查询订单
     * @param orderPageQueryDTO
     * @return
     */
    Page<OrderVO> conditionSearch(OrdersPageQueryDTO orderPageQueryDTO);

    /**
     * 订单统计
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    int countByStatus(Integer status);

    /**
     * 根据状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    /**
     * 查询营业额
     * @param map
     */
    Double sumByMap(Map map);

    @Select("select count(*) from orders where order_time >= #{beginDateTime} and order_time <= #{endDateTime}")
    Integer countTotal(LocalDateTime beginDateTime, LocalDateTime endDateTime);

    @Select("select count(*) from orders where status = 5 and order_time >= #{beginDateTime} and order_time <= #{endDateTime}")
    Integer countByCompleted(LocalDateTime beginDateTime, LocalDateTime endDateTime);

    @Select("SELECT order_detail.name, sum(order_detail.number) as number from orders left join order_detail on orders.id = order_detail.order_id  where order_time >= #{beginDateTime} and order_time <= #{endDateTime} and orders.status = 5 group by order_detail.name order by sum(order_detail.number) desc limit 10")
    List<GoodsSalesDTO> getTop10(LocalDateTime beginDateTime, LocalDateTime endDateTime);

    Integer countByMap(Map map);
}
