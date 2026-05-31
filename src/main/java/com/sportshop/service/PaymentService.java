package com.sportshop.service;

import com.sportshop.models.Order;
import com.sportshop.models.Payment;
import com.sportshop.models.PaymentTransaction;
import com.sportshop.repository.PaymentRepository;

import java.util.List;

public class PaymentService {

    private static final String DEMO_GATEWAY_PROVIDER = "DEMO_GATEWAY";

    private final PaymentRepository paymentRepository =
            new PaymentRepository();

    private final OrderService orderService =
            new OrderService();

    private final NotificationService notificationService =
            new NotificationService();

    public Payment getPaymentById(int paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public Payment getPaymentByOrderId(int orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<PaymentTransaction> getTransactionsByOrderId(int orderId) {
        return paymentRepository.findTransactionsByOrderId(orderId);
    }

    public Payment startPayment(int orderId,
                                int userId,
                                String method) {

        Order order = orderService.getOrder(orderId);

        if (!canPay(order, userId)) {
            return null;
        }

        Payment existingPayment =
                paymentRepository.findByOrderId(orderId);

        if (existingPayment != null) {

            if ("SUCCESS".equals(existingPayment.getStatus())) {
                return existingPayment;
            }

            if ("PENDING".equals(existingPayment.getStatus())) {
                return existingPayment;
            }
        }

        String normalizedMethod = normalizeMethod(method);

        if ("CASH".equals(normalizedMethod)) {
            boolean success =
                    paymentRepository.createCashPaymentAndMarkOrderPaid(
                            order,
                            normalizedMethod
                    );

            if (success) {
                notificationService.notifyUser(
                        userId,
                        "Заказ #" + orderId + " успешно оплачен."
                );

                return paymentRepository.findByOrderId(orderId);
            }

            return null;
        }

        return paymentRepository.createPendingPayment(
                order,
                normalizedMethod,
                DEMO_GATEWAY_PROVIDER
        );
    }

    public boolean processGatewayCallback(int paymentId,
                                          int userId,
                                          String gatewayTransactionId,
                                          String status,
                                          String message) {

        String normalizedStatus =
                "success".equalsIgnoreCase(status) ||
                        "SUCCESS".equalsIgnoreCase(status)
                        ? "SUCCESS"
                        : "FAILED";

        boolean success =
                paymentRepository.completeGatewayPayment(
                        paymentId,
                        userId,
                        normalizeGatewayTransactionId(gatewayTransactionId),
                        normalizedStatus,
                        normalizeMessage(message, normalizedStatus)
                );

        if (success) {
            Payment payment = paymentRepository.findById(paymentId);

            if (payment != null) {
                notificationService.notifyUser(
                        userId,
                        "Заказ #" + payment.getOrderId() +
                                " успешно оплачен через платёжный шлюз."
                );
            }
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
                paymentRepository.refundPaymentAndCancelOrder(
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

    private boolean canPay(Order order, int userId) {
        if (order == null) {
            return false;
        }

        if (order.getUserId() != userId) {
            return false;
        }

        return !"CANCELLED".equals(order.getStatus()) &&
                !"COMPLETED".equals(order.getStatus()) &&
                !"PAID".equals(order.getStatus()) &&
                !"DELIVERING".equals(order.getStatus());
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

    private String normalizeGatewayTransactionId(String value) {
        if (value == null || value.isBlank()) {
            return "GW-" + System.currentTimeMillis();
        }

        return value.trim();
    }

    private String normalizeMessage(String message,
                                    String status) {

        if (message != null && !message.isBlank()) {
            return message.trim();
        }

        if ("SUCCESS".equals(status)) {
            return "Платёж успешно подтверждён платёжным шлюзом";
        }

        return "Платёж отклонён платёжным шлюзом";
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Возврат выполнен администратором";
        }

        return reason.trim();
    }
}
