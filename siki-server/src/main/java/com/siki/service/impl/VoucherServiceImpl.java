package com.siki.service.impl;

import com.siki.constant.MessageConstant;
import com.siki.context.BaseContext;
import com.siki.entity.VoucherOrder;
import com.siki.exception.OrderBusinessException;
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
import com.siki.utils.SimpleRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


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
    public Long addOrder(Long id) {
        // 1.查询代金券
        Voucher voucher = voucherMapper.selectById(id);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
            throw new OrderBusinessException(MessageConstant.HAVENT_START);
        }
        // 3.判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())){
            throw new OrderBusinessException(MessageConstant.ALREADY_END);
        }
        // 4.判断库存是否充足
        if (voucher.getStock() <= 0){
            throw new OrderBusinessException(MessageConstant.NO_STOCK);
        }
        //5.一人一单
        Long currentId = BaseContext.getCurrentId();
        //尝试创建锁对象
//        SimpleRedisLock lock = new SimpleRedisLock("order:" + currentId, redisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + currentId);
        //尝试获取锁
        boolean isLock = lock.tryLock();
        //判断是否获取到锁
        if (!isLock){
            throw new OrderBusinessException(MessageConstant.ALREADY_ORDER);
        }
        //获取代理对象（事物）
        try {
            VoucherService proxy = (VoucherService) AopContext.currentProxy();
            return proxy.createVoucherOrder(id, voucher, currentId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            //释放锁
            lock.unlock();
        }
    }

    @Transactional
    public Long createVoucherOrder(Long id, Voucher voucher, Long currentId) {
        //5.1 查询订单
        int count = voucherOrderMapper.countByUserIdAndVoucherId(currentId, id);
        //5.2 判断是否已经下单
        if (count > 0){
            throw new OrderBusinessException(MessageConstant.ALREADY_ORDER);
        }
        // 6.扣减库存
        boolean success = voucherMapper.reduceStock(id, voucher.getStock());
        if (!success){
            throw new OrderBusinessException(MessageConstant.NO_STOCK);
        }
        // 7.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 7.1 设置订单id
        Long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(currentId);
        // 7.2 设置代金券id
        voucherOrder.setVoucherId(id);
        voucherOrderMapper.addOrder(voucherOrder);
        return orderId;
    }
}
