package com.sportshop.controller;

import com.sportshop.service.NotificationService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationService service =
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
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {

        Integer userId = getUserId(req);

        if (userId == null) {
            resp.sendRedirect("login");
            return;
        }

        String action =
                req.getParameter("action");

        if ("markRead".equals(action)) {
            service.markAllAsRead(userId);
        }

        resp.sendRedirect("profile");
    }
}