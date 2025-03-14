package com.siki.controller.admin;

import ch.qos.logback.core.pattern.util.RestrictedEscapeUtil;
import com.siki.result.Result;
import com.siki.service.ReportService;
import com.siki.vo.OrderReportVO;
import com.siki.vo.SalesTop10ReportVO;
import com.siki.vo.TurnoverReportVO;
import com.siki.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "ReportController", description = "报表管理")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额报表
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额报表")
    public Result<TurnoverReportVO> turnoverReport(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("营业额数据统计：begin:{}, end:{}", begin, end);
        return Result.success(reportService.getTurnoverReport(begin, end));
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数据统计")
    public Result<UserReportVO> userReport(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("用户数据统计：begin:{}, end:{}", begin, end);
        return Result.success(reportService.getUserReport(begin, end));
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数据统计")
    public Result<OrderReportVO> orderReport(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("订单数据统计：begin:{}, end:{}", begin, end);
        return Result.success(reportService.getOrderReport(begin, end));
    }

    @GetMapping("/top10")
    @ApiOperation("销售额Top10商品报表")
    public Result<SalesTop10ReportVO> salesTop10Report(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin, @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end) {
        log.info("销售排行榜：begin:{}, end:{}", begin, end);
        return Result.success(reportService.getSalesTop10Report(begin, end));
    }

    @GetMapping("/export")
    @ApiOperation("导出报表")
    public void export(HttpServletResponse response) {
        reportService.export(response);
    }
}