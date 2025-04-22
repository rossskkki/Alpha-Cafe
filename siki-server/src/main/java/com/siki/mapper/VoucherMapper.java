package com.siki.mapper;

import com.github.pagehelper.Page;
import com.siki.annotation.AutoFill;
import com.siki.dto.VoucherPageQueryDTO;
import com.siki.entity.Voucher;
import com.siki.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface VoucherMapper {

    void insert(Voucher voucher);

    Page<Voucher> selectPage(VoucherPageQueryDTO voucherPageQueryDTO);

    @Update("update voucher set status = #{status} where id = #{id}")
    void updateStatus(Long id, Integer status);

    @Delete("delete from voucher where id = #{id}")
    void delete(Long id);

    @Select("select * from voucher")
    List<Voucher> selectList();
}
