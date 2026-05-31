package com.sportshop.controller;

import com.sportshop.models.Order;
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

        Integer userId = getUserId(req);

        if (userId == null) {
            resp.sendRedirect("login");
            return;
        }

        int orderId =
                parseIntOrDefault(
                        req.getParameter("orderId"),
                        0
                );

        if (orderId <= 0) {
            resp.sendRedirect("orders");
            return;
        }

        Order order =
                orderService.getOrder(orderId);

        if (order == null ||
                order.getUserId() != userId) {

            resp.sendRedirect("orders");
            return;
        }

        req.setAttribute("order", order);

        req.setAttribute(
                "payment",
                paymentService.getPaymentByOrderId(orderId)
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

        int orderId =
                parseIntOrDefault(
                        req.getParameter("orderId"),
                        0
                );

        String method =
                req.getParameter("paymentMethod");

        boolean success =
                paymentService.payOrder(
                        orderId,
                        userId,
                        method
                );

        if (!success) {
            resp.sendRedirect(
                    "payment?orderId=" + orderId + "&error=1"
            );
            return;
        }

        resp.sendRedirect("orders?paid=1");
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