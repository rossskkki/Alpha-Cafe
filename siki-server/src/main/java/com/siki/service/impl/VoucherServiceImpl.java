package com.siki.service.impl;

import com.siki.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.siki.dto.VoucherAddDTO;
import com.siki.dto.VoucherPageQueryDTO;
import com.siki.entity.Voucher;
import com.siki.mapper.VoucherMapper;
import com.siki.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public PageResult page(VoucherPageQueryDTO voucherPageQueryDTO) {
        PageHelper.startPage(voucherPageQueryDTO.getPage(), voucherPageQueryDTO.getPageSize());
        Page<Voucher> page = voucherMapper.selectPage(voucherPageQueryDTO);
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        voucherMapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long id) {
        voucherMapper.delete(id);
    }

    @Override
    public List<Voucher> listVoucher() {
        List<Voucher> list = voucherMapper.selectList();
        return list;
    }
}
