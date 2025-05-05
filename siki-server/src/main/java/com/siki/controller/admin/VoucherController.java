package com.siki.controller.admin;


import com.siki.dto.VoucherAddDTO;
import com.siki.dto.VoucherPageQueryDTO;
import com.siki.result.Result;
import com.siki.service.VoucherService;

import com.siki.result.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/voucher")
@Api(tags = "代金券管理")
@Slf4j
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    /**
     * 添加代金券
     * @param VoucherAddDTO
     * @return
     */
    @PostMapping
    public Result addVoucher(@RequestBody VoucherAddDTO VoucherAddDTO) {
        log.info("添加代金券");
        voucherService.addVoucher(VoucherAddDTO);
        return Result.success();
    }

    /**
     * 分页查询代金券
     * @param voucherPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询代金券")
    public Result<PageResult> page(VoucherPageQueryDTO voucherPageQueryDTO) {
        log.info("分页查询代金券");
        PageResult pageResult = voucherService.page(voucherPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改代金券状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/status/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("修改代金券状态");
        voucherService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除代金券
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        log.info("删除代金券");
        voucherService.delete(id);
        return Result.success();
    }
}
