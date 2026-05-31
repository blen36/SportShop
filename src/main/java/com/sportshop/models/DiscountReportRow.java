package com.sportshop.models;

import java.math.BigDecimal;

public class DiscountReportRow {

    private String promoCode;
    private String discountName;
    private int ordersCount;
    private BigDecimal totalDiscount;

    public DiscountReportRow() {}

    public DiscountReportRow(String promoCode,
                             String discountName,
                             int ordersCount,
                             BigDecimal totalDiscount) {

        this.promoCode = promoCode;
        this.discountName = discountName;
        this.ordersCount = ordersCount;
        this.totalDiscount = totalDiscount;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }


    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }


    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }


    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }
}