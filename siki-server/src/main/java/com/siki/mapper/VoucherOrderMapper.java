package com.siki.mapper;

import com.siki.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VoucherOrderMapper {
    /**
     * 插入订单
     *
     * @param voucherOrder 订单对象
     */
    void addOrder(VoucherOrder voucherOrder);

    @Select("SELECT COUNT(*) FROM voucher_order WHERE user_id = #{currentId} AND voucher_id = #{id}")
    int countByUserIdAndVoucherId(Long currentId, Long id);
}
