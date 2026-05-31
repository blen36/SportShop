package com.sportshop.repository;

import com.sportshop.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RecommendationRepository {

    public void recordView(int userId,
                           int productId) {

        String viewSql = """
            INSERT INTO view_history(
                user_id,
                product_id
            )
            VALUES (?, ?)
        """;

        String activitySql = """
            INSERT INTO user_activity(
                user_id,
                product_id,
                action_type
            )
            VALUES (?, ?, 'VIEW')
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement stmt =
                             conn.prepareStatement(viewSql)) {

                    stmt.setInt(1, userId);
                    stmt.setInt(2, productId);

                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(activitySql)) {

                    stmt.setInt(1, userId);
                    stmt.setInt(2, productId);

                    stmt.executeUpdate();
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}