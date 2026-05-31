package com.sportshop.controller;

import com.sportshop.service.ReviewService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/review")
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService =
            new ReviewService();

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

        int rating =
                parseIntOrDefault(
                        req.getParameter("rating"),
                        0
                );

        String comment =
                req.getParameter("comment");

        boolean success =
                reviewService.addReview(
                        userId,
                        productId,
                        rating,
                        comment
                );

        if (success) {
            resp.sendRedirect(
                    "product?id=" + productId + "&review=success"
            );
        } else {
            resp.sendRedirect(
                    "product?id=" + productId + "&error=review"
            );
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