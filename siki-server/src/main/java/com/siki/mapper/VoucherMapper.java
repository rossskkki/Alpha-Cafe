package com.siki.mapper;

import com.siki.annotation.AutoFill;
import com.siki.entity.Voucher;
import com.siki.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherMapper {

    void insert(Voucher voucher);
}
