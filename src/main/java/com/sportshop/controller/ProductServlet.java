package com.sportshop.controller;

import com.sportshop.service.CategoryService;
import com.sportshop.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private final ProductService productService =
            new ProductService();

    private final CategoryService categoryService =
            new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String query = req.getParameter("q");
        String brand = req.getParameter("brand");
        String categoryParam = req.getParameter("categoryId");
        String sort = req.getParameter("sort");

        Double minPrice = null;
        Double maxPrice = null;
        Double minRating = null;
        Integer categoryId = null;

        int page = 1;
        int limit = 6;

        try {

            String minPriceParam = req.getParameter("minPrice");

            if (minPriceParam != null && !minPriceParam.isBlank()) {
                minPrice = Double.parseDouble(minPriceParam);
            }

            String maxPriceParam = req.getParameter("maxPrice");

            if (maxPriceParam != null && !maxPriceParam.isBlank()) {
                maxPrice = Double.parseDouble(maxPriceParam);
            }

            String minRatingParam = req.getParameter("minRating");

            if (minRatingParam != null && !minRatingParam.isBlank()) {
                minRating = Double.parseDouble(minRatingParam);
            }

            if (categoryParam != null && !categoryParam.isBlank()) {
                categoryId = Integer.parseInt(categoryParam);
            }

            String pageParam = req.getParameter("page");

            if (pageParam != null && !pageParam.isBlank()) {
                page = Integer.parseInt(pageParam);
            }

        } catch (Exception ignored) {
        }

        if (page < 1) {
            page = 1;
        }

        int totalProducts = productService.countWithFilters(
                query,
                brand,
                minPrice,
                maxPrice,
                categoryId,
                minRating
        );

        int totalPages = (int) Math.ceil(totalProducts / (double) limit);

        if (totalPages < 1) {
            totalPages = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        int offset = (page - 1) * limit;

        req.setAttribute(
                "products",
                productService.searchWithFilters(
                        query,
                        brand,
                        minPrice,
                        maxPrice,
                        categoryId,
                        minRating,
                        sort,
                        limit,
                        offset
                )
        );

        req.setAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        req.setAttribute("query", query);
        req.setAttribute("brand", brand);
        req.setAttribute("minPrice", req.getParameter("minPrice"));
        req.setAttribute("maxPrice", req.getParameter("maxPrice"));
        req.setAttribute("minRating", req.getParameter("minRating"));
        req.setAttribute("categoryId", categoryParam);
        req.setAttribute("sort", sort);

        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);

        req.getRequestDispatcher("/products.jsp")
                .forward(req, resp);
    }
}