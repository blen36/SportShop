package com.sportshop.service;

import com.sportshop.models.CartItem;
import com.sportshop.models.Order;
import com.sportshop.models.OrderStatusHistory;
import com.sportshop.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final OrderRepository repo =
            new OrderRepository();

    private final CartService cartService =
            new CartService();

    private final DiscountService discountService =
            new DiscountService();

    private final NotificationService notificationService =
            new NotificationService();

    private static final List<String> ALLOWED_STATUSES = List.of(
            "NEW",
            "CONFIRMED",
            "PAID",
            "DELIVERING",
            "COMPLETED",
            "CANCELLED"
    );

    public int checkout(int userId,
                        String deliveryAddress,
                        String deliveryType,
                        String promoCode) {

        List<CartItem> items = cartService.getCart(userId);

        if (items == null || items.isEmpty()) {
            return 0;
        }

        BigDecimal itemsTotal =
                cartService.calculateTotal(items);

        BigDecimal deliveryPrice =
                calculateDeliveryPrice(
                        deliveryType,
                        itemsTotal
                );

        String normalizedDeliveryAddress =
                normalizeDeliveryAddress(
                        deliveryType,
                        deliveryAddress
                );

        if (normalizedDeliveryAddress == null) {
            return 0;
        }

        BigDecimal promoDiscount =
                discountService.calculatePromoDiscount(
                        itemsTotal,
                        promoCode
                );

        if (promoDiscount == null) {
            return -1;
        }

        String normalizedPromoCode = null;

        if (promoCode != null && !promoCode.isBlank()) {
            normalizedPromoCode = promoCode.trim().toUpperCase();
        }

        int orderId = repo.createOrder(
                userId,
                items,
                normalizedDeliveryAddress,
                deliveryPrice,
                normalizedPromoCode,
                promoDiscount
        );

        if (orderId > 0) {
            notificationService.notifyUser(
                    userId,
                    "Заказ #" + orderId + " создан. Статус: NEW."
            );
        }

        return orderId;
    }

    public BigDecimal calculateDeliveryPrice(String deliveryType,
                                             BigDecimal itemsTotal) {

        if (deliveryType == null ||
                deliveryType.isBlank() ||
                "STANDARD".equals(deliveryType)) {

            if (itemsTotal.compareTo(
                    BigDecimal.valueOf(200)
            ) >= 0) {

                return BigDecimal.ZERO;
            }

            return BigDecimal.valueOf(10);
        }

        if ("EXPRESS".equals(deliveryType)) {
            return BigDecimal.valueOf(20);
        }

        if ("PICKUP".equals(deliveryType)) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(10);
    }

    public String normalizeDeliveryAddress(String deliveryType,
                                           String deliveryAddress) {

        if ("PICKUP".equals(deliveryType)) {
            return "Самовывоз";
        }

        if (deliveryAddress == null ||
                deliveryAddress.isBlank()) {

            return null;
        }

        return deliveryAddress.trim();
    }

    public Order getOrder(int orderId) {
        return repo.findById(orderId);
    }

    public List<Order> getUserOrders(int userId) {
        return repo.getOrdersByUser(userId);
    }

    public List<Order> getAllOrders() {
        return repo.getAllOrders();
    }

    public void updateStatus(int orderId,
                             String status) {
        updateStatus(orderId, status, null);
    }

    public void updateStatus(int orderId,
                             String status,
                             Integer changedByUserId) {

        if (status == null ||
                !ALLOWED_STATUSES.contains(status)) {

            return;
        }

        Order before = repo.findById(orderId);

        if (before == null) {
            return;
        }

        boolean updated = repo.updateStatus(
                orderId,
                status,
                changedByUserId,
                "Статус изменён администратором"
        );

        if (updated &&
                !status.equals(before.getStatus())) {

            notificationService.notifyUser(
                    before.getUserId(),
                    "Статус заказа #" + orderId +
                            " изменён на " + status + "."
            );
        }
    }

    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        return repo.getStatusHistory(orderId);
    }

    public Map<Integer, List<OrderStatusHistory>> getStatusHistoryMap(List<Order> orders) {
        return repo.getStatusHistoryMap(orders);
    }

    public double getTotalRevenue() {
        return repo.getTotalRevenue();
    }

    public String[] getAllowedStatuses() {
        return ALLOWED_STATUSES.toArray(new String[0]);
    }
}
