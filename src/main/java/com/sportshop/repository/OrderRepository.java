package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.CartItem;
import com.sportshop.models.Order;
import com.sportshop.models.OrderStatusHistory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
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

                try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
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

                insertOrderStatusHistory(
                        conn,
                        orderId,
                        null,
                        "NEW",
                        userId,
                        "Заказ создан"
                );

                try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                    for (CartItem item : items) {
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, item.getProductId());
                        stmt.setInt(3, item.getQuantity());
                        stmt.setBigDecimal(4, item.getPrice());
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateInventorySql)) {
                    for (CartItem item : items) {
                        stmt.setInt(1, item.getQuantity());
                        stmt.setInt(2, item.getProductId());
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                try (PreparedStatement stmt = conn.prepareStatement(historySql)) {
                    for (CartItem item : items) {
                        stmt.setInt(1, item.getProductId());
                        stmt.setInt(2, -item.getQuantity());
                        stmt.setString(3, "PURCHASE");
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                try (PreparedStatement stmt = conn.prepareStatement(clearCartSql)) {
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
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
                                String status,
                                Integer changedByUserId,
                                String comment) {

        String selectSql = """
            SELECT status
            FROM orders
            WHERE id=?
            FOR UPDATE
        """;

        String updateSql = """
            UPDATE orders
            SET status=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                String oldStatus;

                try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                    stmt.setInt(1, orderId);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    oldStatus = rs.getString("status");
                }

                if (status.equals(oldStatus)) {
                    conn.commit();
                    return true;
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, status);
                    stmt.setInt(2, orderId);
                    stmt.executeUpdate();
                }

                insertOrderStatusHistory(
                        conn,
                        orderId,
                        oldStatus,
                        status,
                        changedByUserId,
                        comment
                );

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        List<OrderStatusHistory> history = new ArrayList<>();

        String sql = """
            SELECT *
            FROM order_status_history
            WHERE order_id=?
            ORDER BY created_at DESC, id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                history.add(mapStatusHistory(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;
    }

    public Map<Integer, List<OrderStatusHistory>> getStatusHistoryMap(List<Order> orders) {
        Map<Integer, List<OrderStatusHistory>> map = new HashMap<>();

        if (orders == null || orders.isEmpty()) {
            return map;
        }

        for (Order order : orders) {
            map.put(
                    order.getId(),
                    getStatusHistory(order.getId())
            );
        }

        return map;
    }

    public double getTotalRevenue() {
        String sql = """
            SELECT COALESCE(SUM(total_price), 0)
            FROM orders
            WHERE status != 'CANCELLED'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void insertOrderStatusHistory(Connection conn,
                                          int orderId,
                                          String oldStatus,
                                          String newStatus,
                                          Integer changedByUserId,
                                          String comment) throws SQLException {

        String sql = """
            INSERT INTO order_status_history(
                order_id,
                old_status,
                new_status,
                changed_by_user_id,
                comment
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setString(2, oldStatus);
            stmt.setString(3, newStatus);

            if (changedByUserId == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, changedByUserId);
            }

            stmt.setString(5, comment);
            stmt.executeUpdate();
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
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

    private OrderStatusHistory mapStatusHistory(ResultSet rs)
            throws SQLException {

        OrderStatusHistory history = new OrderStatusHistory();

        history.setId(rs.getInt("id"));
        history.setOrderId(rs.getInt("order_id"));
        history.setOldStatus(rs.getString("old_status"));
        history.setNewStatus(rs.getString("new_status"));

        int changedBy = rs.getInt("changed_by_user_id");
        if (rs.wasNull()) {
            history.setChangedByUserId(null);
        } else {
            history.setChangedByUserId(changedBy);
        }

        history.setComment(rs.getString("comment"));
        history.setCreatedAt(rs.getTimestamp("created_at"));

        return history;
    }
}
