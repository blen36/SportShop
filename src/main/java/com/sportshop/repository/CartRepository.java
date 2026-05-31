package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.CartItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    public List<CartItem> getCart(int userId) {

        List<CartItem> items = new ArrayList<>();

        String sql = """
            SELECT
                p.id,
                p.name,
                GREATEST(
                    p.price - COALESCE(
                        SUM(
                            CASE
                                WHEN d.type = 'PERCENT'
                                    THEN p.price * d.value / 100
                                WHEN d.type = 'FIXED'
                                    THEN d.value
                                ELSE 0
                            END
                        ),
                        0
                    ),
                    0
                ) AS final_price,
                c.quantity
            FROM cart_items c
            JOIN products p
                ON c.product_id = p.id

            LEFT JOIN product_discounts pd
                ON p.id = pd.product_id

            LEFT JOIN discounts d
                ON pd.discount_id = d.id
                AND d.active = TRUE
                AND (d.start_date IS NULL OR CURRENT_TIMESTAMP >= d.start_date)
                AND (d.end_date IS NULL OR CURRENT_TIMESTAMP <= d.end_date)

            WHERE c.user_id = ?

            GROUP BY
                c.id,
                p.id,
                p.name,
                p.price,
                c.quantity

            ORDER BY c.id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                items.add(new CartItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("final_price"),
                        rs.getInt("quantity")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public boolean addToCart(int userId, int productId) {

        try (Connection conn = DBConnection.getConnection()) {

            int stock = getStock(conn, productId);
            int currentQty = getCurrentCartQuantity(
                    conn,
                    userId,
                    productId
            );

            if (stock <= 0) {
                return false;
            }

            if (currentQty >= 10 || currentQty >= stock) {
                return false;
            }

            String sql = """
                INSERT INTO cart_items(
                    user_id,
                    product_id,
                    quantity
                )
                VALUES (?, ?, 1)

                ON CONFLICT (user_id, product_id)

                DO UPDATE SET quantity = cart_items.quantity + 1
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setInt(2, productId);

                stmt.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void remove(int userId, int productId) {

        String sql = """
            DELETE FROM cart_items
            WHERE user_id=? AND product_id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateQuantity(int userId,
                                  int productId,
                                  int quantity) {

        if (quantity < 1) {
            quantity = 1;
        }

        if (quantity > 10) {
            quantity = 10;
        }

        try (Connection conn = DBConnection.getConnection()) {

            int stock = getStock(conn, productId);

            if (stock <= 0) {
                return false;
            }

            if (quantity > stock) {
                return false;
            }

            String sql = """
                UPDATE cart_items
                SET quantity=?
                WHERE user_id=? AND product_id=?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, quantity);
                stmt.setInt(2, userId);
                stmt.setInt(3, productId);

                stmt.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void clearCart(int userId) {

        String sql = """
            DELETE FROM cart_items
            WHERE user_id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getStock(Connection conn,
                         int productId) throws SQLException {

        String sql = """
            SELECT quantity
            FROM inventory
            WHERE product_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }

        return 0;
    }

    private int getCurrentCartQuantity(Connection conn,
                                       int userId,
                                       int productId)
            throws SQLException {

        String sql = """
            SELECT quantity
            FROM cart_items
            WHERE user_id=? AND product_id=?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }

        return 0;
    }
}