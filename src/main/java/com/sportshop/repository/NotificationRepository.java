package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    public void create(int userId,
                       String message) {

        String sql = """
            INSERT INTO notifications(
                user_id,
                message
            )
            VALUES (?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, message);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Notification> findByUserId(int userId) {

        List<Notification> notifications = new ArrayList<>();

        String sql = """
            SELECT *
            FROM notifications
            WHERE user_id=?
            ORDER BY created_at DESC
            LIMIT 20
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapNotification(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return notifications;
    }

    public int countUnread(int userId) {

        String sql = """
            SELECT COUNT(*)
            FROM notifications
            WHERE user_id=?
              AND is_read=FALSE
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void markAllAsRead(int userId) {

        String sql = """
            UPDATE notifications
            SET is_read=TRUE
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

    private Notification mapNotification(ResultSet rs)
            throws SQLException {

        Notification notification = new Notification();

        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at"));

        return notification;
    }
}