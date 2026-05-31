package com.sportshop.controller;

import com.sportshop.models.Product;
import com.sportshop.service.CompareService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/compare")
public class CompareServlet extends HttpServlet {

    private final CompareService service =
            new CompareService();

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
        HttpSession session = req.getSession();

        List<Product> products =
                service.getProducts(userId, session);

        Object message =
                session.getAttribute("compareMessage");

        if (message != null) {
            req.setAttribute("compareMessage", message);
            session.removeAttribute("compareMessage");
        }

        req.setAttribute("compareProducts", products);

        req.setAttribute(
                "compareBrandDifferent",
                service.hasDifferentBrands(products)
        );

        req.setAttribute(
                "comparePriceDifferent",
                service.hasDifferentPrices(products)
        );

        req.setAttribute(
                "compareStockDifferent",
                service.hasDifferentStock(products)
        );

        req.setAttribute(
                "compareAttributesDifferent",
                service.hasDifferentAttributes(products)
        );

        req.setAttribute(
                "comparisonSaved",
                userId != null
        );

        req.getRequestDispatcher("/compare.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        Integer userId = getUserId(req);
        HttpSession session = req.getSession();

        String action =
                req.getParameter("action");

        int productId =
                parseIntOrDefault(
                        req.getParameter("productId"),
                        0
                );

        if ("add".equals(action)) {

            boolean success =
                    service.add(
                            userId,
                            session,
                            productId
                    );

            if (!success) {

                session.setAttribute(
                        "compareMessage",
                        "Можно сравнивать не больше 5 товаров."
                );
            }
        }

        else if ("remove".equals(action)) {

            service.remove(
                    userId,
                    session,
                    productId
            );
        }

        else if ("clear".equals(action)) {

            service.clear(
                    userId,
                    session
            );

            resp.sendRedirect("compare");
            return;
        }

        String referer = req.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            resp.sendRedirect("compare");
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