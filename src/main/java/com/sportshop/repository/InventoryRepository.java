package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryRepository {

    public Inventory findByProductId(int productId) {

        String sql = """
            SELECT *
            FROM inventory
            WHERE product_id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return new Inventory(
                        rs.getInt("product_id"),
                        rs.getInt("quantity")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void create(int productId, int quantity) {

        String sql = """
            INSERT INTO inventory(product_id, quantity)
            VALUES (?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, quantity);

            stmt.executeUpdate();

            addHistory(productId, quantity, "RESTOCK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateQuantity(int productId, int quantity) {

        String oldQuantitySql = """
            SELECT quantity
            FROM inventory
            WHERE product_id=?
        """;

        String updateSql = """
            UPDATE inventory
            SET quantity=?
            WHERE product_id=?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            int oldQuantity = 0;

            try (PreparedStatement stmt =
                         conn.prepareStatement(oldQuantitySql)) {

                stmt.setInt(1, productId);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    oldQuantity = rs.getInt("quantity");
                }
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement(updateSql)) {

                stmt.setInt(1, quantity);
                stmt.setInt(2, productId);

                stmt.executeUpdate();
            }

            int change = quantity - oldQuantity;

            if (change != 0) {
                addHistory(productId, change, "RESTOCK");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addHistory(int productId,
                           int changeAmount,
                           String changeType) {

        String sql = """
            INSERT INTO inventory_history(
                product_id,
                change_amount,
                change_type
            )
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, changeAmount);
            stmt.setString(3, changeType);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}