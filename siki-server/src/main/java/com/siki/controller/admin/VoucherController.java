package com.siki.controller.admin;


import com.siki.dto.VoucherAddDTO;
import com.siki.result.Result;
import com.siki.service.VoucherService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/voucher")
@Api(tags = "代金券管理")
@Slf4j
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping
    public Result addVoucher(@RequestBody VoucherAddDTO VoucherAddDTO) {
        log.info("添加代金券");
        voucherService.addVoucher(VoucherAddDTO);
        return Result.success();
    }

}
