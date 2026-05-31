package com.sportshop.service;

import com.sportshop.models.Order;
import com.sportshop.models.Payment;
import com.sportshop.repository.PaymentRepository;

public class PaymentService {

    private final PaymentRepository paymentRepository =
            new PaymentRepository();

    private final OrderService orderService =
            new OrderService();

    private final NotificationService notificationService =
            new NotificationService();

    public Payment getPaymentByOrderId(int orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public boolean payOrder(int orderId,
                            int userId,
                            String method) {

        Order order = orderService.getOrder(orderId);

        if (order == null) {
            return false;
        }

        if (order.getUserId() != userId) {
            return false;
        }

        if ("CANCELLED".equals(order.getStatus()) ||
                "COMPLETED".equals(order.getStatus())) {

            return false;
        }

        Payment existingPayment =
                paymentRepository.findByOrderId(orderId);

        if (existingPayment != null &&
                "SUCCESS".equals(existingPayment.getStatus())) {

            return true;
        }

        String normalizedMethod =
                normalizeMethod(method);

        boolean success =
                paymentRepository
                        .createSuccessfulPaymentAndMarkOrderPaid(
                                order,
                                normalizedMethod
                        );

        if (success) {
            notificationService.notifyUser(
                    userId,
                    "Заказ #" + orderId + " успешно оплачен."
            );
        }

        return success;
    }

    public boolean refundOrder(int orderId,
                               String reason) {

        Order order = orderService.getOrder(orderId);

        if (order == null) {
            return false;
        }

        boolean success =
                paymentRepository
                        .refundPaymentAndCancelOrder(
                                orderId,
                                normalizeReason(reason)
                        );

        if (success) {
            notificationService.notifyUser(
                    order.getUserId(),
                    "По заказу #" + orderId +
                            " выполнен возврат платежа. Заказ отменён."
            );
        }

        return success;
    }

    private String normalizeMethod(String method) {

        if (method == null || method.isBlank()) {
            return "CARD";
        }

        if ("CARD".equals(method) ||
                "CASH".equals(method) ||
                "BANK_TRANSFER".equals(method)) {

            return method;
        }

        return "CARD";
    }

    private String normalizeReason(String reason) {

        if (reason == null || reason.isBlank()) {
            return "Возврат выполнен администратором";
        }

        return reason.trim();
    }
}