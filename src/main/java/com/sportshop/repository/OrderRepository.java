package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.CartItem;
import com.sportshop.models.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public int createOrder(int userId,
                           List<CartItem> items,
                           String deliveryAddress,
                           BigDecimal deliveryPrice,
                           String promoCode,
                           BigDecimal discountAmount) {

        String orderSql = """
            INSERT INTO orders(
                user_id,
                status,
                total_price,
                delivery_address,
                delivery_price,
                promo_code,
                discount_amount
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        String itemSql = """
            INSERT INTO order_items(
                order_id,
                product_id,
                quantity,
                price
            )
            VALUES (?, ?, ?, ?)
        """;

        String stockSql = """
            SELECT quantity
            FROM inventory
            WHERE product_id=?
            FOR UPDATE
        """;

        String updateInventorySql = """
            UPDATE inventory
            SET quantity = quantity - ?
            WHERE product_id = ?
        """;

        String historySql = """
            INSERT INTO inventory_history(
                product_id,
                change_amount,
                change_type
            )
            VALUES (?, ?, ?)
        """;

        String clearCartSql = """
            DELETE FROM cart_items
            WHERE user_id=?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                for (CartItem item : items) {

                    try (PreparedStatement stmt =
                                 conn.prepareStatement(stockSql)) {

                        stmt.setInt(1, item.getProductId());

                        ResultSet rs = stmt.executeQuery();

                        if (!rs.next()) {
                            conn.rollback();
                            return 0;
                        }

                        int stock = rs.getInt("quantity");

                        if (stock < item.getQuantity()) {
                            conn.rollback();
                            return 0;
                        }
                    }
                }

                BigDecimal itemsTotal = items.stream()
                        .map(CartItem::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (discountAmount == null) {
                    discountAmount = BigDecimal.ZERO;
                }

                BigDecimal finalTotal =
                        itemsTotal
                                .add(deliveryPrice)
                                .subtract(discountAmount);

                if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
                    finalTotal = BigDecimal.ZERO;
                }

                int orderId;

                try (PreparedStatement stmt =
                             conn.prepareStatement(orderSql)) {

                    stmt.setInt(1, userId);
                    stmt.setString(2, "NEW");
                    stmt.setBigDecimal(3, finalTotal);
                    stmt.setString(4, deliveryAddress);
                    stmt.setBigDecimal(5, deliveryPrice);

                    if (promoCode == null || promoCode.isBlank()) {
                        stmt.setNull(6, Types.VARCHAR);
                    } else {
                        stmt.setString(6, promoCode.trim().toUpperCase());
                    }

                    stmt.setBigDecimal(7, discountAmount);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return 0;
                    }

                    orderId = rs.getInt("id");
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(itemSql)) {

                    for (CartItem item : items) {

                        stmt.setInt(1, orderId);
                        stmt.setInt(2, item.getProductId());
                        stmt.setInt(3, item.getQuantity());
                        stmt.setBigDecimal(4, item.getPrice());

                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(updateInventorySql)) {

                    for (CartItem item : items) {

                        stmt.setInt(1, item.getQuantity());
                        stmt.setInt(2, item.getProductId());

                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(historySql)) {

                    for (CartItem item : items) {

                        stmt.setInt(1, item.getProductId());
                        stmt.setInt(2, -item.getQuantity());
                        stmt.setString(3, "PURCHASE");

                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(clearCartSql)) {

                    stmt.setInt(1, userId);

                    stmt.executeUpdate();
                }

                conn.commit();

                return orderId;

            } catch (Exception e) {

                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public Order findById(int orderId) {

        String sql = """
            SELECT *
            FROM orders
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapOrder(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Order> getOrdersByUser(int userId) {

        List<Order> orders = new ArrayList<>();

        String sql = """
            SELECT *
            FROM orders
            WHERE user_id = ?
            ORDER BY created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();

        String sql = """
            SELECT *
            FROM orders
            ORDER BY created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public boolean updateStatus(int orderId,
                                String status) {

        String sql = """
            UPDATE orders
            SET status=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public double getTotalRevenue() {

        String sql = """
            SELECT COALESCE(SUM(total_price), 0)
            FROM orders
            WHERE status != 'CANCELLED'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Order mapOrder(ResultSet rs)
            throws SQLException {

        Order order = new Order();

        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setDeliveryPrice(rs.getBigDecimal("delivery_price"));
        order.setPromoCode(rs.getString("promo_code"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));

        return order;
    }
}