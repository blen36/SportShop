package com.sportshop.controller;

import com.sportshop.service.OrderService;
import com.sportshop.service.RecommendationService;
import com.sportshop.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserService userService =
            new UserService();

    private final OrderService orderService =
            new OrderService();

    private final RecommendationService recommendationService =
            new RecommendationService();

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
                "user",
                userService.getUser(userId)
        );

        req.setAttribute(
                "favorites",
                userService.getFavorites(userId)
        );

        req.setAttribute(
                "orders",
                orderService.getUserOrders(userId)
        );

        req.setAttribute(
                "recentlyViewed",
                recommendationService.getRecentlyViewed(
                        userId,
                        6
                )
        );

        req.setAttribute(
                "recommendations",
                recommendationService.getRecommendations(
                        userId,
                        6
                )
        );

        req.getRequestDispatcher("/profile.jsp")
                .forward(req, resp);
    }
}