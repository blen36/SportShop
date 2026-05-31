package com.sportshop.controller;

import com.sportshop.models.CartItem;
import com.sportshop.service.CartService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final CartService service = new CartService();

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

        List<CartItem> cart =
                service.getCart(userId);

        BigDecimal cartTotal =
                service.calculateTotal(cart);

        req.setAttribute("cart", cart);
        req.setAttribute("cartTotal", cartTotal);

        req.getRequestDispatcher("/cart.jsp")
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

        String action = req.getParameter("action");

        int productId =
                parseIntOrDefault(
                        req.getParameter("productId"),
                        0
                );

        if (productId <= 0) {
            resp.sendRedirect("cart");
            return;
        }

        if ("add".equals(action)) {

            boolean success =
                    service.add(userId, productId);

            if (!success) {
                resp.sendRedirect("products?error=stock");
                return;
            }
        }

        else if ("remove".equals(action)) {

            service.remove(userId, productId);
        }

        else if ("update".equals(action)) {

            int qty =
                    parseIntOrDefault(
                            req.getParameter("quantity"),
                            1
                    );

            boolean success =
                    service.update(
                            userId,
                            productId,
                            qty
                    );

            if (!success) {
                resp.sendRedirect("cart?error=stock");
                return;
            }
        }

        resp.sendRedirect("cart");
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