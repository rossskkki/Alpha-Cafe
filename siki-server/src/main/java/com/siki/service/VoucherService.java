package com.siki.service;

import com.siki.dto.VoucherAddDTO;
import com.siki.dto.VoucherPageQueryDTO;
import com.siki.entity.Voucher;
import com.siki.entity.VoucherOrder;
import com.siki.result.PageResult;

import java.util.List;

public interface VoucherService {
    void addVoucher(VoucherAddDTO voucherAddDTO);

    PageResult page(VoucherPageQueryDTO voucherPageQueryDTO);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    List<Voucher> listVoucher();

    Long addOrder(Long id);

    Voucher getVoucherById(Long id);

    void createVoucherOrder(VoucherOrder voucherOrder);
}
