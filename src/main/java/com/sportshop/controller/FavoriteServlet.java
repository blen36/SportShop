package com.sportshop.controller;

import com.sportshop.service.UserService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/favorite")
public class FavoriteServlet extends HttpServlet {

    private final UserService service =
            new UserService();

    private Integer getUserId(HttpServletRequest req) {

        Object userId =
                req.getSession().getAttribute("userId");

        if (userId instanceof Integer) {
            return (Integer) userId;
        }

        return null;
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

        int productId =
                parseIntOrDefault(
                        req.getParameter("productId"),
                        0
                );

        if (productId <= 0) {
            resp.sendRedirect("products");
            return;
        }

        String action =
                req.getParameter("action");

        if ("add".equals(action)) {
            service.addFavorite(userId, productId);
        }

        else if ("remove".equals(action)) {
            service.removeFavorite(userId, productId);
        }

        String referer = req.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            resp.sendRedirect("profile");
        } else {
            resp.sendRedirect(referer);
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