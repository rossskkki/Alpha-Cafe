package com.siki.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.siki.constant.MessageConstant;
import com.siki.context.BaseContext;
import com.siki.dto.*;
import com.siki.entity.*;
import com.siki.exception.AddressBookBusinessException;
import com.siki.exception.OrderBusinessException;
import com.siki.mapper.*;
import com.siki.result.PageResult;
import com.siki.service.OrderService;
import com.siki.utils.HttpClientUtil;
import com.siki.utils.WeChatPayUtil;
import com.siki.vo.OrderPaymentVO;
import com.siki.vo.OrderStatisticsVO;
import com.siki.vo.OrderSubmitVO;
import com.siki.vo.OrderVO;
import com.siki.websocket.WebSocketServer;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.siki.constant.RedisConstants.PICKUP_CODE_KEY;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

//    @Autowired
//    private AddressBookMapper addressBookMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    private Orders orders;

    @Value("${siki.shop.address}")
    private String shopAddress;

    @Value("${siki.baidu.ak}")
    private String ak;

    private static final String getLngAndLatURL= "https://api.map.baidu.com/geocoding/v3";

    public static final String getDistanceURL="https://api.map.baidu.com/directionlite/v1/driving";


    /**
     * 生成取餐码
     * @return 3位数的取餐码
     */
    private String generatePickupCode() {
        // 获取当前日期作为key的一部分
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = PICKUP_CODE_KEY + today;

        // 原子性递增并获取值
        Long increment = redisTemplate.opsForValue().increment(key);

        // 如果是第一次设置，添加过期时间（到第二天凌晨）
        if (increment == 1) {
            // 设置key在第二天凌晨过期
            LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
            long secondsUntilTomorrow = ChronoUnit.SECONDS.between(LocalDateTime.now(), tomorrow);
            redisTemplate.expire(key, secondsUntilTomorrow, TimeUnit.SECONDS);
        }

        // 处理超过999的情况
        if (increment > 999) {
            // 重置为1
            redisTemplate.opsForValue().set(key, "1");
            increment = 1L;
        }

        // 格式化为3位数的字符串
        return String.format("%03d", increment);
    }

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常（地址簿为空，购物车数据为空）
//        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
//        if (addressBook == null) {
//            //抛出异常
//            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
//        }

        //检查收货地址是否超出配送范围
//        checkOutOfRange(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());


        //查询购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            //抛出异常
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        //取餐码
        orders.setPickupCode(generatePickupCode());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(userMapper.getPhoneById(userId));
//        orders.setConsignee(addressBook.getConsignee());
//        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setUserId(userId);
        this.orders = orders;
        orderMapper.insert(orders);
        //订单详情表插入多条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .pickupCode(orders.getPickupCode())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, this.orders.getId());

        // 通过WebSocket向客户端发送消息
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", this.orders.getId());
        map.put("content", "订单号："+ this.orders.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询订单
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        Long currentId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(currentId);
        ordersPageQueryDTO.setStatus(status);

        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> result = new ArrayList<>();

        if (page != null && page.size() > 0) {
            for (Orders order : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                Long orderId = order.getId();
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                orderVO.setOrderDetailList(orderDetails);
                result.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), result);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    public OrderVO getOrderDetail(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        Orders order = orderMapper.getById(id);
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     *
     * @param id
     */
    public void cancelOrder(Long id) {
        orderMapper.cancel(id);
    }

    /**
     * 重复下单
     *
     * @param id
     */
    public void repetOrder(Long id) {
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 分页查询订单
     * @param orderPageQueryDTO
     * @return
     */
    public PageResult conditionSearch(OrdersPageQueryDTO orderPageQueryDTO) {
        PageHelper.startPage(orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.conditionSearch(orderPageQueryDTO);
        List<OrderVO> orderVOList = page.getResult();
        for (OrderVO orderVO : orderVOList) {
            List<OrderDetail> orderdishesList = orderDetailMapper.getByOrderId(orderVO.getId());
            // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
            List<String> orderDishList = orderdishesList.stream().map(x -> {
                String orderDish = x.getName() + "*" + x.getNumber() + ";";
                return orderDish;
            }).collect(Collectors.toList());

            // 将该订单对应的所有菜品信息拼接在一起
            String orderDishes = String.join("", orderDishList);
            orderVO.setOrderDishes(orderDishes);
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 订单统计
     * @return
     */
    public OrderStatisticsVO statistics() {
        int comfirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        int deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        int toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .confirmed(comfirmed)
                .deliveryInProgress(deliveryInProgress)
                .toBeConfirmed(toBeConfirmed)
                .build();
        return orderStatisticsVO;
    }

    /**
     * 订单详情
     * @param id
     * @return
     */
    public OrderVO detail(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 确认订单
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersConfirmDTO, orders);
        orderMapper.update(orders);
    }

    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     */
    public void rejuct(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO, orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     * @return
     */
    public void cancelOrderByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO, orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @return
     */
    public void delivery(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @return
     */
    public void complete(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setFinishedTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 订单提醒
     * @param id
     */
//    public void orderRemind(Long id) {
//        //根据id查询订单
//        Orders orders = orderMapper.getById(id);
//
//        //校验订单是否存在
//        if(orders == null){
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
//        }
//
//        Map map = new HashMap();
//        map.put("type", 2);
//        map.put("orderId", id);
//        map.put("content", "订单号："+ orders.getNumber());
//
//        String json = JSON.toJSONString(map);
//        webSocketServer.sendToAllClient(json);
//    }
//
//    /**
//     * 检查客户的收货地址是否超出配送范围
//     * @param address
//     */
//    private void checkOutOfRange(String address) {
//        Map map = new HashMap();
//        map.put("address",shopAddress);
//        map.put("output","json");
//        map.put("ak",ak);
//
//        //获取店铺的经纬度坐标
//        String shopCoordinate = HttpClientUtil.doGet(getLngAndLatURL, map);
//
//        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
//        if(!jsonObject.getString("status").equals("0")){
//            throw new OrderBusinessException("店铺地址解析失败");
//        }
//
//        //数据解析
//        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
//        String lat = location.getString("lat");
//        String lng = location.getString("lng");
//        //店铺经纬度坐标
//        String shopLngLat = lat + "," + lng;
//
//        map.put("address",address);
//        //获取用户收货地址的经纬度坐标
//        String userCoordinate = HttpClientUtil.doGet(getLngAndLatURL, map);
//
//        jsonObject = JSON.parseObject(userCoordinate);
//        if(!jsonObject.getString("status").equals("0")){
//            throw new OrderBusinessException("收货地址解析失败");
//        }
//
//        //数据解析
//        location = jsonObject.getJSONObject("result").getJSONObject("location");
//        lat = location.getString("lat");
//        lng = location.getString("lng");
//        //用户收货地址经纬度坐标
//        String userLngLat = lat + "," + lng;
//
//        map.put("origin",shopLngLat);
//        map.put("destination",userLngLat);
//        map.put("steps_info","0");
//
//        //路线规划
//        String json = HttpClientUtil.doGet(getDistanceURL, map);
//
//        jsonObject = JSON.parseObject(json);
//        if(!jsonObject.getString("status").equals("0")){
//            throw new OrderBusinessException("配送路线规划失败");
//        }
//
//        //数据解析
//        JSONObject result = jsonObject.getJSONObject("result");
//        JSONArray jsonArray = (JSONArray) result.get("routes");
//        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");
//
//        if(distance > 5000){
//            //配送距离超过5000米
//            throw new OrderBusinessException("超出配送范围");
//        }
//    }

}
