package com.siki.controller.user;

import com.siki.entity.Voucher;
import com.siki.result.Result;
import com.siki.service.VoucherService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userVoucherController")
@RequestMapping("/user/voucher")
@ApiOperation("用户优惠券管理")
@Slf4j
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/seckill/list")
    public Result<List<Voucher>> listVoucher(){
        List<Voucher> list = voucherService.listVoucher();
        return Result.success(list);
    }

    @PostMapping("/order/{id}")
    public Result<Long> addVoucherOrder(@PathVariable("id") Long voucherId) {
        Long orderId = voucherService.addOrder(voucherId);
        return Result.success(orderId);
    }
}
