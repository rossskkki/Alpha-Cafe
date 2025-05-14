package com.siki.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.siki.constant.RedisConstants.SECKILL_STOCK_KEY;

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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private VoucherService proxy;

    //lua脚本配置
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static{
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    // 阻塞队列
    //private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    // 创建单线程池
//    private static  final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
//
//    @PostConstruct
//    private void init() {
//        // 启动线程
//        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
//    }


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
        // 保存到redis`
        redisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }

    /**
     * 查询代金券列表
     * @param voucherPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(VoucherPageQueryDTO voucherPageQueryDTO) {
        PageHelper.startPage(voucherPageQueryDTO.getPage(), voucherPageQueryDTO.getPageSize());
        Page<Voucher> page = voucherMapper.selectPage(voucherPageQueryDTO);
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    /**
     * 更新代金券状态
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        voucherMapper.updateStatus(id, status);
    }

    /**
     * 更新代金券
     * @param id
     */
    @Override
    public void delete(Long id) {
        voucherMapper.delete(id);
        // 删除redis中的代金券
        redisTemplate.delete(SECKILL_STOCK_KEY + id);
    }

    /**
     * 用户端展示代金券
     * @return list
     */
    @Override
    public List<Voucher> listVoucher() {
        List<Voucher> list = voucherMapper.selectList();
        return list;
    }

    /**
     * 用户端查询代金券详情
     * @param id
     * @return
     */
    @Override
    public Voucher getVoucherById(Long id) {
        return voucherMapper.selectById(id);
    }

    /**
     * 秒杀下单(stream消息队列)
     * @param id
     * @return
     */
    public Long addOrder(Long id) {
        Long currentId = BaseContext.getCurrentId();
        //创建订单id
        long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result =  redisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), id.toString(), currentId.toString(), String.valueOf(orderId));
        // 2. 判断结果是否为0
        int r = result.intValue();

        if (r != 0){
            // 2.1 如果不为0，说明秒杀失败，抛出异常
            throw new OrderBusinessException(r == 1 ? MessageConstant.NO_STOCK : MessageConstant.ALREADY_ORDER);
        }
        // 2.2 如果为0，说明秒杀成功，继续执行
        // 2.3. 将订单放入消息队列
        VoucherOrder voucherOrder = new VoucherOrder();
        // 2.3.1 设置订单
        voucherOrder.setId(orderId);
        // 2.3.2 设置用户id
        voucherOrder.setUserId(currentId);
        // 2.3.3 设置代金券id
        voucherOrder.setVoucherId(id);
        try {
            // 发送消息
            rabbitTemplate.convertAndSend("order.direct", "create", voucherOrder);
        } catch (AmqpException e) {
            throw new RuntimeException(e);
        }
        // 2.4 获取代理对象
        proxy = (VoucherService) AopContext.currentProxy();
        // 3. 返回订单id
        return orderId;
    }

    /**
     * 秒杀下单(阻塞队列)
     * @param id
     * @return
     */
//    @Override
//    public Long addOrder(Long id) {
//        Long currentId = BaseContext.getCurrentId();
//        long orderId;
//        // 1.执行lua脚本
//        Long result =  redisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(), id.toString(), currentId.toString());
//        // 2. 判断结果是否为0
//        int r = result.intValue();
//
//        if (r != 0){
//            // 2.1 如果不为0，说明秒杀失败，抛出异常
//            throw new OrderBusinessException(r == 1 ? MessageConstant.NO_STOCK : MessageConstant.ALREADY_ORDER);
//        }
//        // 2.2 如果为0，说明秒杀成功，继续执行
//        orderId = redisIdWorker.nextId("order");
//        // 3. 将订单放入阻塞队列
//        VoucherOrder voucherOrder = new VoucherOrder();
//        // 3.1 设置订单
//        voucherOrder.setId(orderId);
//        // 3.2 设置用户id
//        voucherOrder.setUserId(currentId);
//        // 3.3 设置代金券id
//        voucherOrder.setVoucherId(id);
//        orderTasks.add(voucherOrder);
//        // 3.4 获取代理对象
//        proxy = (VoucherService) AopContext.currentProxy();
//        // 3. 返回订单id
//        return orderId;
//    }

    /**
     * 秒杀下单(常规)
     * @param
     * @return
     */
//    @Override
//    public Long addOrder(Long id) {
//        // 1.查询代金券
//        Voucher voucher = voucherMapper.selectById(id);
//        // 2.判断秒杀是否开始
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())){
//            throw new OrderBusinessException(MessageConstant.HAVENT_START);
//        }
//        // 3.判断秒杀是否结束
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())){
//            throw new OrderBusinessException(MessageConstant.ALREADY_END);
//        }
//        // 4.判断库存是否充足
//        if (voucher.getStock() <= 0){
//            throw new OrderBusinessException(MessageConstant.NO_STOCK);
//        }
//        //5.一人一单
//        Long currentId = BaseContext.getCurrentId();
//        //尝试创建锁对象
////        SimpleRedisLock lock = new SimpleRedisLock("order:" + currentId, redisTemplate);
//        RLock lock = redissonClient.getLock("lock:order:" + currentId);
//        //尝试获取锁
//        boolean isLock = lock.tryLock();
//        //判断是否获取到锁
//        if (!isLock){
//            throw new OrderBusinessException(MessageConstant.ALREADY_ORDER);
//        }
//        //获取代理对象（事物）
//        try {
//            VoucherService proxy = (VoucherService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(id, voucher, currentId);
//        } catch (IllegalStateException e) {
//            throw new RuntimeException(e);
//        } finally {
//            //释放锁
//            lock.unlock();
//        }
//    }

    //rabbitmq消息队列监听
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name  = "order.create", durable = "true"),
            exchange = @Exchange(name = "order.direct"),
            key = "create"
    ))
    public void VoucherOrderHandler (VoucherOrder voucherOrder) {
        // 1.创建订单
        handleVoucherOrder(voucherOrder);
    }

    //异步线程操作
//    private class VoucherOrderHandler implements Runnable {
//        String queueName = "stream.orders";
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 1.获取订单
//                    List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(
//                            Consumer.from("g1", "c1"),
//                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
//                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
//                    );
//                    // 2.判断消息获取是否成功
//                    if (list == null || list.isEmpty()){
//                        continue;
//                    }
//                    // 3.解析消息
//                    MapRecord<String, Object, Object> record = list.get(0);
//                    Map<Object, Object> value = record.getValue();
//                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
//                    // 3.创建订单
//                    handleVoucherOrder(voucherOrder);
//                    //4.ACK确认
//                    redisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
//                } catch (Exception e) {
//                    log.error("处理订单失败", e);
//                    // 5.失败重试
//                    handlePendingList();
//                }
//            }
//        }
//        // 处理pending-list中的消息
//        private void handlePendingList() {
//            while(true){
//                try {
//                    // 1.获取pending-list中的消息
//                    List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(
//                            Consumer.from("g1", "c1"),
//                            StreamReadOptions.empty().count(1),
//                            StreamOffset.create(queueName, ReadOffset.from("0"))
//                    );
//                    // 2.判断消息获取是否成功
//                    if (list == null || list.isEmpty()) {
//                        break;
//                    }
//                    // 3.解析消息
//                    MapRecord<String, Object, Object> record = list.get(0);
//                    Map<Object, Object> value = record.getValue();
//                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
//                    // 4.创建订单
//                    handleVoucherOrder(voucherOrder);
//                    //5.ACK确认
//                    redisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
//                } catch (Exception e) {
//                   log.error("处理pending-list中的订单失败", e);
//                    // 6.失败重试
//                    try {
//                        Thread.sleep(20);
//                    } catch (InterruptedException ex) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//            }
//        }
//    }

    //异步线程操作（阻塞队列）
//    private class VoucherOrderHandler implements Runnable {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 1.获取订单
//                    VoucherOrder voucherOrder = orderTasks.take();
//                    // 2.创建订单
//                    handleVoucherOrder(voucherOrder);
//                } catch (Exception e) {
//                    log.error("处理订单失败", e);
//                }
//            }
//        }
//    }

    //异步获取锁加判断
    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //尝试获取锁
        boolean isLock = lock.tryLock();
        //判断是否获取到锁
        if (!isLock){
            log.error("获取锁失败，用户id：{}", userId);
            return;
        }
        //获取代理对象（事物）
        try {
            proxy.createVoucherOrder(voucherOrder);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            //释放锁
            lock.unlock();
        }
    }



    //创建订单
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //5.1 查询订单
        int count = voucherOrderMapper.countByUserIdAndVoucherId(voucherOrder.getUserId(), voucherOrder.getVoucherId());
        //5.2 判断是否已经下单
        if (count > 0){
            log.error("用户已经下单，用户id：{}，代金券id：{}", voucherOrder.getUserId(), voucherOrder.getVoucherId());
            return;
        }
        // 6.扣减库存
        boolean success = voucherMapper.reduceStock(voucherOrder.getVoucherId());
        if (!success){
            log.error("库存不足，代金券id：{}", voucherOrder.getVoucherId());
//            throw new OrderBusinessException(MessageConstant.NO_STOCK);
            return;
        }
        // 7.1 创建订单
        voucherOrderMapper.addOrder(voucherOrder);
    }
}
