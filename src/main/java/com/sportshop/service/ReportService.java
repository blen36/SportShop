package com.sportshop.service;

import com.sportshop.models.DiscountReportRow;
import com.sportshop.models.PopularProductReportRow;
import com.sportshop.models.SalesReportRow;
import com.sportshop.repository.ReportRepository;

import java.math.BigDecimal;
import java.util.List;

public class ReportService {

    private final ReportRepository repository =
            new ReportRepository();

    public List<SalesReportRow> getSalesByDay() {
        return repository.getSalesByDay(14);
    }

    public List<PopularProductReportRow> getPopularProducts() {
        return repository.getPopularProducts(10);
    }

    public List<DiscountReportRow> getDiscountEffectiveness() {
        return repository.getDiscountEffectiveness();
    }

    public BigDecimal getRefundedAmount() {
        return repository.getRefundedAmount();
    }
}