package com.siki.controller.user;

import com.siki.entity.Voucher;
import com.siki.result.Result;
import com.siki.service.VoucherService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
