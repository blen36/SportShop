package com.sportshop.controller;

import com.sportshop.models.Order;
import com.sportshop.service.NotificationService;
import com.sportshop.service.OrderService;
import com.sportshop.service.RecommendationService;
import com.sportshop.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserService userService =
            new UserService();

    private final OrderService orderService =
            new OrderService();

    private final RecommendationService recommendationService =
            new RecommendationService();

    private final NotificationService notificationService =
            new NotificationService();

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

        List<Order> orders = orderService.getUserOrders(userId);

        req.setAttribute(
                "user",
                userService.getUser(userId)
        );

        req.setAttribute(
                "favorites",
                userService.getFavorites(userId)
        );

        req.setAttribute("orders", orders);

        req.setAttribute(
                "orderStatusHistory",
                orderService.getStatusHistoryMap(orders)
        );

        req.setAttribute(
                "notifications",
                notificationService.getUserNotifications(userId)
        );

        req.setAttribute(
                "unreadNotifications",
                notificationService.getUnreadCount(userId)
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
