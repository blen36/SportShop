package com.sportshop.controller;

import com.sportshop.models.Category;
import com.sportshop.models.Discount;
import com.sportshop.models.Order;
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

        HttpSession session = req.getSession();

        Object adminMessage = session.getAttribute("adminMessage");
        Object adminError = session.getAttribute("adminError");

        if (adminMessage != null) {
            req.setAttribute("adminMessage", adminMessage);
            session.removeAttribute("adminMessage");
        }

        if (adminError != null) {
            req.setAttribute("adminError", adminError);
            session.removeAttribute("adminError");
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

        if ("editCategory".equals(action)) {
            int id = parseIntOrDefault(
                    req.getParameter("id"),
                    0
            );

            if (id > 0) {
                req.setAttribute(
                        "category",
                        categoryService.getCategory(id)
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

        List<Order> orders = orderService.getAllOrders();

        req.setAttribute("products", products);
        req.setAttribute("lowStockProducts", lowStockProducts);
        req.setAttribute("categories", categoryService.getAllCategories());

        req.setAttribute("orders", orders);
        req.setAttribute("orderStatuses", orderService.getAllowedStatuses());
        req.setAttribute(
                "orderStatusHistory",
                orderService.getStatusHistoryMap(orders)
        );

        req.setAttribute("users", userService.getAllUsers());
        req.setAttribute("reviews", reviewService.getAllReviews());

        req.setAttribute("discounts", discountService.getAllDiscounts());

        req.setAttribute("productsCount", productService.getProductsCount());
        req.setAttribute("ordersCount", orders.size());
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

        HttpSession session = req.getSession();
        String action = req.getParameter("action");

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
            int stock = parseIntOrDefault(
                    req.getParameter("bulkStock"),
                    0
            );

            for (Integer id : parseIds(req.getParameter("productIds"))) {
                inventoryService.saveOrUpdate(id, stock);
            }
        }

        else if ("updateStatus".equals(action)) {
            int orderId = parseIntOrDefault(
                    req.getParameter("orderId"),
                    0
            );

            String status = req.getParameter("status");

            if (orderId > 0) {
                orderService.updateStatus(
                        orderId,
                        status,
                        getCurrentUserId(req)
                );
            }
        }

        else if ("refundOrder".equals(action)) {
            int orderId = parseIntOrDefault(
                    req.getParameter("orderId"),
                    0
            );

            String reason = req.getParameter("reason");

            if (orderId > 0) {
                boolean success = paymentService.refundOrder(orderId, reason);

                if (!success) {
                    session.setAttribute(
                            "adminError",
                            "Не удалось выполнить возврат. Проверьте, что заказ оплачен и не завершён."
                    );
                }
            }
        }

        else if ("saveDiscount".equals(action)) {
            saveDiscount(req);
        }

        else if ("deleteDiscount".equals(action)) {
            int id = parseIntOrDefault(
                    req.getParameter("id"),
                    0
            );

            if (id > 0) {
                discountService.delete(id);
            }
        }

        else if ("saveCategory".equals(action)) {
            int categoryId = saveCategory(req);

            if (categoryId > 0) {
                session.setAttribute(
                        "adminMessage",
                        "Категория сохранена."
                );
            } else {
                session.setAttribute(
                        "adminError",
                        "Не удалось сохранить категорию. Проверьте название и родительскую категорию."
                );
            }
        }

        else if ("deleteCategory".equals(action)) {
            int id = parseIntOrDefault(
                    req.getParameter("id"),
                    0
            );

            if (id > 0) {
                boolean deleted = categoryService.delete(id);

                if (deleted) {
                    session.setAttribute(
                            "adminMessage",
                            "Категория удалена."
                    );
                } else {
                    session.setAttribute(
                            "adminError",
                            "Категорию нельзя удалить, если у неё есть подкатегории или товары."
                    );
                }
            }
        }

        else if ("updateUser".equals(action)) {
            int userId = parseIntOrDefault(
                    req.getParameter("userId"),
                    0
            );

            String role = req.getParameter("role");

            boolean blocked =
                    "on".equals(req.getParameter("blocked"));

            Integer currentUserId = getCurrentUserId(req);

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
            int reviewId = parseIntOrDefault(
                    req.getParameter("reviewId"),
                    0
            );

            String status = req.getParameter("status");

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

        int stock = parseIntOrDefault(
                req.getParameter("stock"),
                0
        );

        String idParam = req.getParameter("id");

        if (idParam != null &&
                !idParam.isBlank()) {

            product.setId(Integer.parseInt(idParam));

            productService.update(product);

            inventoryService.saveOrUpdate(
                    product.getId(),
                    stock
            );

        } else {

            int productId = productService.create(product);

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

    private int saveCategory(HttpServletRequest req) {
        Category category = new Category();

        category.setId(
                parseIntOrDefault(
                        req.getParameter("id"),
                        0
                )
        );

        category.setName(req.getParameter("name"));

        int parentId = parseIntOrDefault(
                req.getParameter("parentId"),
                0
        );

        if (parentId > 0) {
            category.setParentId(parentId);
        }

        return categoryService.save(category);
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

            String normalized = value.trim().replace("T", " ");

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

        String[] parts = value.split("[,;\\s]+");

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

        StringBuilder builder = new StringBuilder();

        for (Integer id : ids) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(id);
        }

        return builder.toString();
    }
}
