package com.sportshop.repository;

import com.sportshop.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComparisonRepository {

    public List<Integer> findProductIds(int userId) {

        List<Integer> ids = new ArrayList<>();

        String sql = """
            SELECT product_id
            FROM comparison_items
            WHERE user_id = ?
            ORDER BY added_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("product_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    public boolean addProduct(int userId,
                              int productId) {

        List<Integer> ids = findProductIds(userId);

        if (ids.contains(productId)) {
            return true;
        }

        if (ids.size() >= 5) {
            return false;
        }

        String sql = """
            INSERT INTO comparison_items(
                user_id,
                product_id
            )
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            stmt.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void removeProduct(int userId,
                              int productId) {

        String sql = """
            DELETE FROM comparison_items
            WHERE user_id = ?
              AND product_id = ?
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

    public void clear(int userId) {

        String sql = """
            DELETE FROM comparison_items
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}