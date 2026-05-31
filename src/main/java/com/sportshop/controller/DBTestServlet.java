package com.sportshop.controller;

import com.sportshop.config.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;

public class DBTestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                resp.getWriter().println("<h1>Связь с PostgreSQL установлена! 🚀</h1>");
            }
        } catch (Exception e) {
            resp.getWriter().println("<h1>Ошибка подключения:</h1>");
            resp.getWriter().println("<pre>" + e.getMessage() + "</pre>");
            e.printStackTrace();
        }
    }
}