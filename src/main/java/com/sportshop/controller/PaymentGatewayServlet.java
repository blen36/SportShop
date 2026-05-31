package com.sportshop.controller;

import com.sportshop.models.Payment;
import com.sportshop.service.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.UUID;

@WebServlet("/payment-gateway")
public class PaymentGatewayServlet extends HttpServlet {

    private final PaymentService paymentService =
            new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        int paymentId = parseIntOrDefault(
                req.getParameter("paymentId"),
                0
        );

        Payment payment = paymentService.getPaymentById(paymentId);

        if (payment == null || !"PENDING".equals(payment.getStatus())) {
            resp.sendRedirect("orders?paymentError=1");
            return;
        }

        Object userId = req.getSession().getAttribute("userId");

        if (userId instanceof Integer &&
                payment.getUserId() != (Integer) userId) {

            resp.sendRedirect("orders?paymentError=1");
            return;
        }

        req.setAttribute("payment", payment);

        resp.setHeader("Cache-Control", "no-store");
        req.getRequestDispatcher("/payment_gateway.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        int paymentId = parseIntOrDefault(
                req.getParameter("paymentId"),
                0
        );

        String result = req.getParameter("result");

        String gatewayTransactionId =
                "GW-" + UUID.randomUUID();

        String status = "fail".equals(result) ? "FAILED" : "SUCCESS";

        String message = "SUCCESS".equals(status)
                ? "Демо-шлюз подтвердил платёж"
                : "Демо-шлюз отклонил платёж";

        resp.sendRedirect(
                "payment?action=callback" +
                        "&paymentId=" + paymentId +
                        "&gatewayTransactionId=" + gatewayTransactionId +
                        "&status=" + status +
                        "&message=" + java.net.URLEncoder.encode(
                        message,
                        java.nio.charset.StandardCharsets.UTF_8
                )
        );
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
