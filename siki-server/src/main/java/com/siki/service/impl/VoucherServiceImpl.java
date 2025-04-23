package com.siki.service.impl;

import com.siki.context.BaseContext;
import com.siki.entity.VoucherOrder;
import com.siki.mapper.VoucherOrderMapper;
import com.siki.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.siki.dto.VoucherAddDTO;
import com.siki.dto.VoucherPageQueryDTO;
import com.siki.entity.Voucher;
import com.siki.mapper.VoucherMapper;
import com.siki.service.VoucherService;
import com.siki.utils.RedisIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private VoucherOrderMapper voucherOrderMapper;

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

    @Override
    public Voucher getVoucherById(Long id) {
        return voucherMapper.selectById(id);
    }

    @Override
    @Transactional
    public Long addOrder(Long id) {
        // 1.查询代金券
        Voucher voucher = voucherMapper.selectById(id);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
            throw new RuntimeException("秒杀未开始");
        }
        // 3.判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())){
            throw new RuntimeException("秒杀已结束");
        }
        // 4.判断库存是否充足
        if (voucher.getStock() <= 0){
            throw new RuntimeException("第一次库存不足");
        }
        // 5.扣减库存
        boolean success = voucherMapper.reduceStock(id);
        if (!success){
            throw new RuntimeException("第二次库存不足");
        }
        // 6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //6.1 设置订单id
        Long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //6.2 设置用户id
        Long currentId = BaseContext.getCurrentId();
        voucherOrder.setUserId(currentId);
        //6.3 设置代金券id
        voucherOrder.setVoucherId(id);
        voucherOrderMapper.addOrder(voucherOrder);
        return orderId;
    }
}
