package com.sportshop.models;

import java.math.BigDecimal;

public class PopularProductReportRow {

    private int productId;
    private String productName;
    private int soldQuantity;
    private BigDecimal revenue;

    public PopularProductReportRow() {}

    public PopularProductReportRow(int productId,
                                   String productName,
                                   int soldQuantity,
                                   BigDecimal revenue) {

        this.productId = productId;
        this.productName = productName;
        this.soldQuantity = soldQuantity;
        this.revenue = revenue;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }


    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}