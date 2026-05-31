package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Order;
import com.sportshop.models.Payment;

import java.sql.*;

public class PaymentRepository {

    public Payment findByOrderId(int orderId) {

        String sql = """
            SELECT *
            FROM payments
            WHERE order_id=?
            ORDER BY created_at DESC
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapPayment(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean createSuccessfulPaymentAndMarkOrderPaid(Order order,
                                                           String method) {

        String paymentSql = """
            INSERT INTO payments(
                order_id,
                user_id,
                amount,
                payment_method,
                status
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        String updateOrderSql = """
            UPDATE orders
            SET status='PAID'
            WHERE id=?
              AND user_id=?
              AND status NOT IN ('CANCELLED', 'COMPLETED')
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement stmt =
                             conn.prepareStatement(paymentSql)) {

                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, order.getUserId());
                    stmt.setBigDecimal(3, order.getTotalPrice());
                    stmt.setString(4, method);
                    stmt.setString(5, "SUCCESS");

                    stmt.executeUpdate();
                }

                int updatedRows;

                try (PreparedStatement stmt =
                             conn.prepareStatement(updateOrderSql)) {

                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, order.getUserId());

                    updatedRows = stmt.executeUpdate();
                }

                if (updatedRows == 0) {
                    conn.rollback();
                    return false;
                }

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

    public boolean refundPaymentAndCancelOrder(int orderId,
                                               String reason) {

        String refundPaymentSql = """
            UPDATE payments
            SET
                status='REFUNDED',
                refund_reason=?,
                refunded_at=CURRENT_TIMESTAMP
            WHERE order_id=?
              AND status='SUCCESS'
        """;

        String cancelOrderSql = """
            UPDATE orders
            SET status='CANCELLED'
            WHERE id=?
              AND status NOT IN ('COMPLETED', 'CANCELLED')
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                int paymentRows;

                try (PreparedStatement stmt =
                             conn.prepareStatement(refundPaymentSql)) {

                    stmt.setString(1, reason);
                    stmt.setInt(2, orderId);

                    paymentRows = stmt.executeUpdate();
                }

                if (paymentRows == 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(cancelOrderSql)) {

                    stmt.setInt(1, orderId);
                    stmt.executeUpdate();
                }

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

    private Payment mapPayment(ResultSet rs)
            throws SQLException {

        Payment payment = new Payment();

        payment.setId(rs.getInt("id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setStatus(rs.getString("status"));
        payment.setCreatedAt(rs.getTimestamp("created_at"));

        return payment;
    }
}