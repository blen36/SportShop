package com.sportshop.controller;

import com.sportshop.models.Discount;
import com.sportshop.models.Product;
import com.sportshop.service.CategoryService;
import com.sportshop.service.DiscountService;
import com.sportshop.service.InventoryService;
import com.sportshop.service.OrderService;
import com.sportshop.service.PaymentService;
import com.sportshop.service.ProductService;
import com.sportshop.service.ReportService;
import com.sportshop.service.ReviewService;
import com.sportshop.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private final ProductService productService =
            new ProductService();

    private final CategoryService categoryService =
            new CategoryService();

    private final OrderService orderService =
            new OrderService();

    private final UserService userService =
            new UserService();

    private final InventoryService inventoryService =
            new InventoryService();

    private final DiscountService discountService =
            new DiscountService();

    private final PaymentService paymentService =
            new PaymentService();

    private final ReviewService reviewService =
            new ReviewService();

    private final ReportService reportService =
            new ReportService();

    private boolean isAdmin(HttpServletRequest req) {

        String role =
                (String) req.getSession()
                        .getAttribute("role");

        return role != null && role.equals("ADMIN");
    }

    private Integer getCurrentUserId(HttpServletRequest req) {

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

        if (!isAdmin(req)) {
            resp.sendRedirect("login");
            return;
        }

        String action = req.getParameter("action");

        if ("editProduct".equals(action)) {

            int id = parseIntOrDefault(
                    req.getParameter("id"),
                    0
            );

            if (id > 0) {
                req.setAttribute(
                        "product",
                        productService.getProduct(id)
                );
            }
        }

        if ("editDiscount".equals(action)) {

            int id = parseIntOrDefault(
                    req.getParameter("id"),
                    0
            );

            if (id > 0) {

                req.setAttribute(
                        "discount",
                        discountService.getDiscount(id)
                );

                List<Integer> productIds =
                        discountService.getProductIdsByDiscount(id);

                req.setAttribute(
                        "discountProductIdsText",
                        idsToText(productIds)
                );
            }
        }

        List<Product> products =
                productService.getAllProducts();

        List<Product> lowStockProducts =
                products.stream()
                        .filter(p -> p.getStock() > 0 &&
                                p.getStock() <= 5)
                        .toList();

        req.setAttribute("products", products);
        req.setAttribute("lowStockProducts", lowStockProducts);
        req.setAttribute("categories", categoryService.getAllCategories());

        req.setAttribute("orders", orderService.getAllOrders());
        req.setAttribute("orderStatuses", orderService.getAllowedStatuses());

        req.setAttribute("users", userService.getAllUsers());
        req.setAttribute("reviews", reviewService.getAllReviews());

        req.setAttribute("discounts", discountService.getAllDiscounts());

        req.setAttribute("productsCount", productService.getProductsCount());
        req.setAttribute("ordersCount", orderService.getAllOrders().size());
        req.setAttribute("usersCount", userService.getUsersCount());
        req.setAttribute("totalRevenue", orderService.getTotalRevenue());

        req.setAttribute("salesReport", reportService.getSalesByDay());
        req.setAttribute("popularReport", reportService.getPopularProducts());
        req.setAttribute("discountReport", reportService.getDiscountEffectiveness());
        req.setAttribute("refundedAmount", reportService.getRefundedAmount());

        req.getRequestDispatcher("/admin.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding("UTF-8");

        if (!isAdmin(req)) {
            resp.sendRedirect("login");
            return;
        }

        String action =
                req.getParameter("action");

        if ("saveProduct".equals(action)) {
            saveProduct(req);
        }

        else if ("deleteProduct".equals(action)) {

            int id = parseIntOrDefault(
                    req.getParameter("id"),
                    0
            );

            if (id > 0) {
                productService.delete(id);
            }
        }

        else if ("bulkDeleteProducts".equals(action)) {

            for (Integer id : parseIds(req.getParameter("productIds"))) {
                productService.delete(id);
            }
        }

        else if ("bulkSetStock".equals(action)) {

            int stock =
                    parseIntOrDefault(
                            req.getParameter("bulkStock"),
                            0
                    );

            for (Integer id : parseIds(req.getParameter("productIds"))) {
                inventoryService.saveOrUpdate(id, stock);
            }
        }

        else if ("updateStatus".equals(action)) {

            int orderId =
                    parseIntOrDefault(
                            req.getParameter("orderId"),
                            0
                    );

            String status =
                    req.getParameter("status");

            if (orderId > 0) {
                orderService.updateStatus(orderId, status);
            }
        }

        else if ("refundOrder".equals(action)) {

            int orderId =
                    parseIntOrDefault(
                            req.getParameter("orderId"),
                            0
                    );

            String reason =
                    req.getParameter("reason");

            if (orderId > 0) {
                paymentService.refundOrder(orderId, reason);
            }
        }

        else if ("saveDiscount".equals(action)) {
            saveDiscount(req);
        }

        else if ("deleteDiscount".equals(action)) {

            int id =
                    parseIntOrDefault(
                            req.getParameter("id"),
                            0
                    );

            if (id > 0) {
                discountService.delete(id);
            }
        }

        else if ("updateUser".equals(action)) {

            int userId =
                    parseIntOrDefault(
                            req.getParameter("userId"),
                            0
                    );

            String role =
                    req.getParameter("role");

            boolean blocked =
                    "on".equals(req.getParameter("blocked"));

            Integer currentUserId =
                    getCurrentUserId(req);

            if (userId > 0) {

                userService.updateRole(userId, role);

                if (currentUserId == null ||
                        userId != currentUserId ||
                        !blocked) {

                    userService.updateBlocked(userId, blocked);
                }
            }
        }

        else if ("updateReviewStatus".equals(action)) {

            int reviewId =
                    parseIntOrDefault(
                            req.getParameter("reviewId"),
                            0
                    );

            String status =
                    req.getParameter("status");

            if (reviewId > 0) {
                reviewService.updateStatus(reviewId, status);
            }
        }

        resp.sendRedirect("admin");
    }

    private void saveProduct(HttpServletRequest req) {

        Product product = new Product();

        product.setName(req.getParameter("name"));
        product.setDescription(req.getParameter("description"));

        product.setPrice(
                new BigDecimal(
                        req.getParameter("price")
                )
        );

        product.setBrand(req.getParameter("brand"));

        String categoryIdParam =
                req.getParameter("categoryId");

        if (categoryIdParam != null &&
                !categoryIdParam.isBlank()) {

            product.setCategoryId(
                    Integer.parseInt(categoryIdParam)
            );
        }

        product.setAttributes(req.getParameter("attributes"));
        product.setImageUrl(req.getParameter("imageUrl"));

        int stock =
                parseIntOrDefault(
                        req.getParameter("stock"),
                        0
                );

        String idParam =
                req.getParameter("id");

        if (idParam != null &&
                !idParam.isBlank()) {

            product.setId(
                    Integer.parseInt(idParam)
            );

            productService.update(product);

            inventoryService.saveOrUpdate(
                    product.getId(),
                    stock
            );

        } else {

            int productId =
                    productService.create(product);

            if (productId > 0) {
                inventoryService.saveOrUpdate(
                        productId,
                        stock
                );
            }
        }
    }

    private void saveDiscount(HttpServletRequest req) {

        Discount discount = new Discount();

        discount.setId(
                parseIntOrDefault(
                        req.getParameter("id"),
                        0
                )
        );

        discount.setName(req.getParameter("name"));
        discount.setType(req.getParameter("type"));

        discount.setValue(
                new BigDecimal(
                        req.getParameter("value")
                )
        );

        discount.setStartDate(
                parseTimestamp(req.getParameter("startDate"))
        );

        discount.setEndDate(
                parseTimestamp(req.getParameter("endDate"))
        );

        discount.setCode(req.getParameter("code"));
        discount.setActive("on".equals(req.getParameter("active")));

        List<Integer> productIds =
                parseIds(req.getParameter("productIds"));

        discountService.save(discount, productIds);
    }

    private int parseIntOrDefault(String value,
                                  int defaultValue) {

        try {

            if (value == null || value.isBlank()) {
                return defaultValue;
            }

            return Integer.parseInt(value.trim());

        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Timestamp parseTimestamp(String value) {

        try {

            if (value == null || value.isBlank()) {
                return null;
            }

            String normalized =
                    value.trim().replace("T", " ");

            if (normalized.length() == 16) {
                normalized += ":00";
            }

            return Timestamp.valueOf(normalized);

        } catch (Exception e) {
            return null;
        }
    }

    private List<Integer> parseIds(String value) {

        List<Integer> ids = new ArrayList<>();

        if (value == null || value.isBlank()) {
            return ids;
        }

        String[] parts =
                value.split("[,;\\s]+");

        for (String part : parts) {

            try {

                if (!part.isBlank()) {
                    ids.add(Integer.parseInt(part.trim()));
                }

            } catch (Exception ignored) {
            }
        }

        return ids;
    }

    private String idsToText(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            return "";
        }

        StringBuilder builder =
                new StringBuilder();

        for (Integer id : ids) {

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(id);
        }

        return builder.toString();
    }
}