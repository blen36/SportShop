package com.sportshop.controller;

import com.sportshop.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private final OrderService service =
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

        req.setAttribute(
                "orders",
                service.getUserOrders(userId)
        );

        req.getRequestDispatcher("/orders.jsp")
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

        String deliveryAddress =
                req.getParameter("deliveryAddress");

        String deliveryType =
                req.getParameter("deliveryType");

        String promoCode =
                req.getParameter("promoCode");

        int orderId =
                service.checkout(
                        userId,
                        deliveryAddress,
                        deliveryType,
                        promoCode
                );

        if (orderId == -1) {
            resp.sendRedirect("cart?error=promo");
            return;
        }

        if (orderId <= 0) {
            resp.sendRedirect("cart?error=checkout");
            return;
        }

        resp.sendRedirect("payment?orderId=" + orderId);
    }
}