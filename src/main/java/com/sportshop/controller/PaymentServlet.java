package com.sportshop.controller;

import com.sportshop.models.Order;
import com.sportshop.models.Payment;
import com.sportshop.service.OrderService;
import com.sportshop.service.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    private final PaymentService paymentService =
            new PaymentService();

    private final OrderService orderService =
            new OrderService();

    private Integer getUserId(HttpServletRequest req) {
        Object userId =
                req.getSession().getAttribute("userId");

        if (userId instanceof Integer) {
            return (Integer) userId;
        }

        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        Integer userId = getUserId(req);

        if (userId == null) {
            resp.sendRedirect("login");
            return;
        }

        String action = req.getParameter("action");

        if ("callback".equals(action)) {
            processCallback(req, resp, userId);
            return;
        }

        int orderId = parseIntOrDefault(
                req.getParameter("orderId"),
                0
        );

        if (orderId <= 0) {
            resp.sendRedirect("orders");
            return;
        }

        Order order = orderService.getOrder(orderId);

        if (order == null || order.getUserId() != userId) {
            resp.sendRedirect("orders");
            return;
        }

        Payment payment = paymentService.getPaymentByOrderId(orderId);

        req.setAttribute("order", order);
        req.setAttribute("payment", payment);
        req.setAttribute(
                "paymentTransactions",
                paymentService.getTransactionsByOrderId(orderId)
        );

        req.getRequestDispatcher("/payment.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        Integer userId = getUserId(req);

        if (userId == null) {
            resp.sendRedirect("login");
            return;
        }

        int orderId = parseIntOrDefault(
                req.getParameter("orderId"),
                0
        );

        String method = req.getParameter("paymentMethod");

        Payment payment = paymentService.startPayment(
                orderId,
                userId,
                method
        );

        if (payment == null) {
            resp.sendRedirect(
                    "payment?orderId=" + orderId + "&error=1"
            );
            return;
        }

        if ("SUCCESS".equals(payment.getStatus())) {
            resp.sendRedirect("orders?paid=1");
            return;
        }

        resp.sendRedirect("payment-gateway?paymentId=" + payment.getId());
    }

    private void processCallback(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 int userId) throws IOException {

        int paymentId = parseIntOrDefault(
                req.getParameter("paymentId"),
                0
        );

        String gatewayTransactionId =
                req.getParameter("gatewayTransactionId");

        String status = req.getParameter("status");
        String message = req.getParameter("message");

        boolean success = paymentService.processGatewayCallback(
                paymentId,
                userId,
                gatewayTransactionId,
                status,
                message
        );

        Payment payment = paymentService.getPaymentById(paymentId);

        if (success) {
            resp.sendRedirect("orders?paid=1");
            return;
        }

        if (payment != null) {
            resp.sendRedirect(
                    "payment?orderId=" + payment.getOrderId() + "&error=1"
            );
        } else {
            resp.sendRedirect("orders?paymentError=1");
        }
    }

    private int parseIntOrDefault(String value,
                                  int defaultValue) {

        try {
            if (value == null || value.isBlank()) {
                return defaultValue;
            }

            return Integer.parseInt(value);

        } catch (Exception e) {
            return defaultValue;
        }
    }
}
