package com.sportshop.controller;

import com.sportshop.models.Product;
import com.sportshop.service.ProductService;
import com.sportshop.service.RecommendationService;
import com.sportshop.service.ReviewService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/product")
public class ProductDetailsServlet extends HttpServlet {

    private final ProductService productService =
            new ProductService();

    private final ReviewService reviewService =
            new ReviewService();

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

        req.setCharacterEncoding("UTF-8");

        int productId =
                parseIntOrDefault(
                        req.getParameter("id"),
                        0
                );

        if (productId <= 0) {
            resp.sendRedirect("products");
            return;
        }

        Product product =
                productService.getProduct(productId);

        if (product == null) {
            resp.sendRedirect("products");
            return;
        }

        Integer userId = getUserId(req);

        recommendationService.recordProductView(
                userId,
                productId
        );

        boolean canReview = false;

        if (userId != null) {
            canReview =
                    reviewService.canUserReview(
                            userId,
                            productId
                    );
        }

        req.setAttribute("product", product);

        req.setAttribute(
                "reviews",
                reviewService.getProductReviews(productId)
        );

        req.setAttribute("canReview", canReview);

        req.setAttribute(
                "relatedProducts",
                productService.getRelatedProducts(product, 4)
        );

        req.setAttribute(
                "popularProducts",
                recommendationService.getPopularProducts(4)
        );

        req.getRequestDispatcher("/product.jsp")
                .forward(req, resp);
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