package com.siki.mapper;

import com.siki.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherOrderMapper {
    /**
     * 插入订单
     *
     * @param voucherOrder 订单对象
     */
    void addOrder(VoucherOrder voucherOrder);
}
