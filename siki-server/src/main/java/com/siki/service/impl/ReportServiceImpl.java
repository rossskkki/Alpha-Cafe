package com.siki.service.impl;

import com.siki.dto.GoodsSalesDTO;
import com.siki.entity.Orders;
import com.siki.mapper.OrderMapper;
import com.siki.mapper.UserMapper;
import com.siki.service.ReportService;
import com.siki.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceServiceImpl workspaceService;

    /**
     * 营业额报表
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate day = begin;
        while (!day.equals(end)) {
            dateList.add(day);
            day = day.plusDays(1);
        }
        dateList.add(end);
        String datelist = StringUtils.join(dateList, ",");

        // 查询营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
           turnover = turnover == null ? 0 : turnover;
            turnoverList.add(turnover);
        }
        String turnover = StringUtils.join(turnoverList, ",");
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder().dateList(datelist).turnoverList(turnover).build();
        return turnoverReportVO;
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate day = begin;
        while (!day.equals(end)) {
            dateList.add(day);
            day = day.plusDays(1);
        }
        dateList.add(end);
        String datelist = StringUtils.join(dateList, ",");

        // 查询用户数量及新增用户数量
        List<Integer> userList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer userCount = userMapper.countUserByMap(map);
            Integer newUserCount = userMapper.countNewUserByMap(map);
            userCount = userCount == null ? 0 : userCount;
            userList.add(userCount);
            newUserList.add(newUserCount);
        }
        String user = StringUtils.join(userList, ",");
        String newUser = StringUtils.join(newUserList, ",");

        //封住返回数据
        UserReportVO userReportVO = UserReportVO.builder().dateList(datelist).totalUserList(user).newUserList(newUser).build();
        return userReportVO;
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate day = begin;
        while (!day.equals(end)) {
            dateList.add(day);
            day = day.plusDays(1);
        }
        dateList.add(end);
        String datelist = StringUtils.join(dateList, ",");

        // 查询订单及完成订单总数
        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);
        Integer totalOrder = orderMapper.countTotal(beginDateTime, endDateTime);
        Integer totalCompletedOrder = orderMapper.countByCompleted(beginDateTime, endDateTime);

        //查询某天的订单数量
        List<Integer> orderList = new ArrayList<>();
        List<Integer> completedOrderList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = orderMapper.countTotal(beginTime, endTime);
            Integer completedOrderCount = orderMapper.countByCompleted(beginTime, endTime);
            orderList.add(orderCount);
            completedOrderList.add(completedOrderCount);
        }
        String order = StringUtils.join(orderList, ",");
        String completedOrder = StringUtils.join(completedOrderList, ",");
        Double orderCompletionRate = totalOrder == 0 ? 0 : totalCompletedOrder * 1.0 / totalOrder;
        OrderReportVO orderReportVO = OrderReportVO.builder().dateList(datelist).orderCountList(order).validOrderCountList(completedOrder).totalOrderCount(totalOrder).validOrderCount(totalCompletedOrder).orderCompletionRate(orderCompletionRate).build();
        return orderReportVO;
    }

    /**
     * 销售额Top10商品报表
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10Report(LocalDate begin, LocalDate end) {
        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> top10List = orderMapper.getTop10(beginDateTime, endDateTime);

        List<String> dishNameList = top10List.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String dishName = StringUtils.join(dishNameList, ",");

        List<Integer> salesList = top10List.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String sales = StringUtils.join(salesList, ",");

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder().nameList(dishName).numberList(sales).build();
        return salesTop10ReportVO;
    }

    /**
     * 导出报表
     * @param response
     */
    public void export(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("templete/运营数据报表模板.xlsx");
        try {

            XSSFWorkbook excel = new XSSFWorkbook(resourceAsStream);
            excel.getSheetAt(0).getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            excel.getSheetAt(0).getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            excel.getSheetAt(0).getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            excel.getSheetAt(0).getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            excel.getSheetAt(0).getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            excel.getSheetAt(0).getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO daysdata = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = excel.getSheetAt(0).getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(daysdata.getTurnover());
                row.getCell(3).setCellValue(daysdata.getValidOrderCount());
                row.getCell(4).setCellValue(daysdata.getOrderCompletionRate());
                row.getCell(5).setCellValue(daysdata.getUnitPrice());
                row.getCell(6).setCellValue(daysdata.getNewUsers());
            }

            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
