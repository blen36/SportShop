package com.sportshop.models;

import java.math.BigDecimal;

public class SalesReportRow {

    private String period;
    private int ordersCount;
    private BigDecimal revenue;

    public SalesReportRow() {}

    public SalesReportRow(String period,
                          int ordersCount,
                          BigDecimal revenue) {

        this.period = period;
        this.ordersCount = ordersCount;
        this.revenue = revenue;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }


    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }


    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}