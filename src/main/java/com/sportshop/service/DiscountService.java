package com.sportshop.service;

import com.sportshop.models.Discount;
import com.sportshop.repository.DiscountRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DiscountService {

    private final DiscountRepository repository =
            new DiscountRepository();

    public List<Discount> getAllDiscounts() {
        return repository.findAll();
    }

    public Discount getDiscount(int id) {
        return repository.findById(id);
    }

    public Discount getValidPromo(String code) {
        return repository.findActivePromoByCode(code);
    }

    public int save(Discount discount,
                    List<Integer> productIds) {

        normalizeDiscount(discount);

        int discountId =
                repository.save(discount);

        if (discountId > 0) {
            repository.replaceProducts(
                    discountId,
                    productIds
            );
        }

        return discountId;
    }

    public void delete(int id) {
        repository.delete(id);
    }

    public List<Integer> getProductIdsByDiscount(int discountId) {
        return repository.findProductIdsByDiscountId(discountId);
    }

    public BigDecimal calculatePromoDiscount(BigDecimal amount,
                                             String promoCode) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        if (promoCode == null ||
                promoCode.isBlank()) {

            return BigDecimal.ZERO;
        }

        Discount discount =
                repository.findActivePromoByCode(promoCode);

        if (discount == null) {
            return null;
        }

        BigDecimal result = BigDecimal.ZERO;

        if ("PERCENT".equals(discount.getType())) {

            result = amount
                    .multiply(discount.getValue())
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        else if ("FIXED".equals(discount.getType())) {

            result = discount.getValue();
        }

        if (result.compareTo(amount) > 0) {
            result = amount;
        }

        if (result.compareTo(BigDecimal.ZERO) < 0) {
            result = BigDecimal.ZERO;
        }

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private void normalizeDiscount(Discount discount) {

        if (discount.getName() != null) {
            discount.setName(discount.getName().trim());
        }

        if (discount.getType() == null ||
                !discount.getType().equals("FIXED")) {

            discount.setType("PERCENT");
        }

        if (discount.getValue() == null ||
                discount.getValue().compareTo(BigDecimal.ZERO) < 0) {

            discount.setValue(BigDecimal.ZERO);
        }

        if ("PERCENT".equals(discount.getType()) &&
                discount.getValue()
                        .compareTo(BigDecimal.valueOf(100)) > 0) {

            discount.setValue(BigDecimal.valueOf(100));
        }

        if (discount.getCode() != null &&
                !discount.getCode().isBlank()) {

            discount.setCode(
                    discount.getCode()
                            .trim()
                            .toUpperCase()
            );
        }
    }
}