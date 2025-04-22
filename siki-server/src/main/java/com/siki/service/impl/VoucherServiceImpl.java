package com.siki.service.impl;

import com.siki.dto.VoucherAddDTO;
import com.siki.entity.Voucher;
import com.siki.mapper.VoucherMapper;
import com.siki.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherMapper voucherMapper;
    /**
     * 添加代金券
     * @param voucherAddDTO
     */
    @Override
    @Transactional
    public void addVoucher(VoucherAddDTO voucherAddDTO) {
        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherAddDTO, voucher);
        voucherMapper.insert(voucher);
    }
}
