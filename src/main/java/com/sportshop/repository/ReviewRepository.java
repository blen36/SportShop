package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {

    public List<Review> findApprovedByProductId(int productId) {

        List<Review> reviews = new ArrayList<>();

        String sql = """
            SELECT
                r.*,
                u.email AS user_email
            FROM reviews r
            JOIN users u
                ON r.user_id = u.id
            WHERE r.product_id = ?
              AND r.status = 'APPROVED'
            ORDER BY r.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(mapReview(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reviews;
    }

    public List<Review> findAll() {

        List<Review> reviews = new ArrayList<>();

        String sql = """
            SELECT
                r.*,
                u.email AS user_email
            FROM reviews r
            JOIN users u
                ON r.user_id = u.id
            ORDER BY r.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reviews.add(mapReview(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reviews;
    }

    public boolean addReview(int userId,
                             int productId,
                             int rating,
                             String comment) {

        if (!canUserReview(userId, productId)) {
            return false;
        }

        String sql = """
            INSERT INTO reviews(
                user_id,
                product_id,
                rating,
                comment,
                status
            )
            VALUES (?, ?, ?, ?, 'APPROVED')
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);

            stmt.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean canUserReview(int userId,
                                 int productId) {

        return hasPurchasedProduct(userId, productId)
                && !hasUserReviewedProduct(userId, productId);
    }

    public boolean hasPurchasedProduct(int userId,
                                       int productId) {

        String sql = """
            SELECT COUNT(*)
            FROM orders o
            JOIN order_items oi
                ON o.id = oi.order_id
            WHERE o.user_id = ?
              AND oi.product_id = ?
              AND o.status IN (
                    'PAID',
                    'DELIVERING',
                    'COMPLETED'
              )
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasUserReviewedProduct(int userId,
                                          int productId) {

        String sql = """
            SELECT COUNT(*)
            FROM reviews
            WHERE user_id = ?
              AND product_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, productId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void updateStatus(int reviewId,
                             String status) {

        String sql = """
            UPDATE reviews
            SET status=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, reviewId);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Review mapReview(ResultSet rs)
            throws SQLException {

        Review review = new Review();

        review.setId(rs.getInt("id"));
        review.setUserId(rs.getInt("user_id"));
        review.setUserEmail(rs.getString("user_email"));
        review.setProductId(rs.getInt("product_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setStatus(rs.getString("status"));
        review.setCreatedAt(rs.getTimestamp("created_at"));

        return review;
    }
}