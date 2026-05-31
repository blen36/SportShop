package com.sportshop.models;

import java.math.BigDecimal;

public class DiscountCalculation {

    private String promoCode;
    private BigDecimal discountAmount;
    private boolean valid;

    public DiscountCalculation() {
        this.promoCode = null;
        this.discountAmount = BigDecimal.ZERO;
        this.valid = false;
    }

    public DiscountCalculation(String promoCode,
                               BigDecimal discountAmount,
                               boolean valid) {

        this.promoCode = promoCode;
        this.discountAmount = discountAmount;
        this.valid = valid;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }


    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }


    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}