package com.siki.service;

import com.siki.vo.OrderReportVO;
import com.siki.vo.SalesTop10ReportVO;
import com.siki.vo.TurnoverReportVO;
import com.siki.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    /**
     * 营业额报表
     *
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end);

    /**
     * 用户数据统计
     *
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserReport(LocalDate begin, LocalDate end);

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderReport(LocalDate begin, LocalDate end);

    /**
     * 销售额Top10商品报表
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10Report(LocalDate begin, LocalDate end);

    void export(HttpServletResponse response);
}
