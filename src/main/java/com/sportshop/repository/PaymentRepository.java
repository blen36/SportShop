package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Order;
import com.sportshop.models.Payment;
import com.sportshop.models.PaymentTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {

    public Payment findById(int paymentId) {
        String sql = """
            SELECT *
            FROM payments
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapPayment(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Payment findByOrderId(int orderId) {
        String sql = """
            SELECT *
            FROM payments
            WHERE order_id=?
            ORDER BY created_at DESC, id DESC
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    public List<PaymentTransaction> findTransactionsByOrderId(int orderId) {
        List<PaymentTransaction> transactions = new ArrayList<>();

        String sql = """
            SELECT *
            FROM payment_transactions
            WHERE order_id=?
            ORDER BY created_at DESC, id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public Payment createPendingPayment(Order order,
                                        String method,
                                        String provider) {

        String paymentSql = """
            INSERT INTO payments(
                order_id,
                user_id,
                amount,
                payment_method,
                status,
                provider
            )
            VALUES (?, ?, ?, ?, 'PENDING', ?)
            RETURNING *
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Payment payment;

                try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, order.getUserId());
                    stmt.setBigDecimal(3, order.getTotalPrice());
                    stmt.setString(4, method);
                    stmt.setString(5, provider);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return null;
                    }

                    payment = mapPayment(rs);
                }

                insertTransaction(
                        conn,
                        payment.getId(),
                        order.getId(),
                        order.getUserId(),
                        "PAYMENT",
                        "PENDING",
                        order.getTotalPrice(),
                        provider,
                        null,
                        "Платёж создан и ожидает подтверждения платёжного шлюза"
                );

                conn.commit();
                return payment;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean createCashPaymentAndMarkOrderPaid(Order order,
                                                     String method) {

        String paymentSql = """
            INSERT INTO payments(
                order_id,
                user_id,
                amount,
                payment_method,
                status,
                provider,
                gateway_transaction_id
            )
            VALUES (?, ?, ?, ?, 'SUCCESS', 'CASH_ON_DELIVERY', ?)
            RETURNING id
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                String oldStatus = lockOrderAndGetStatus(conn, order.getId());

                if (oldStatus == null ||
                        "CANCELLED".equals(oldStatus) ||
                        "COMPLETED".equals(oldStatus)) {

                    conn.rollback();
                    return false;
                }

                String transactionId =
                        "CASH-" + System.currentTimeMillis() + "-" + order.getId();

                int paymentId;

                try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, order.getUserId());
                    stmt.setBigDecimal(3, order.getTotalPrice());
                    stmt.setString(4, method);
                    stmt.setString(5, transactionId);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    paymentId = rs.getInt("id");
                }

                updateOrderStatus(conn, order.getId(), "PAID");

                insertOrderStatusHistory(
                        conn,
                        order.getId(),
                        oldStatus,
                        "PAID",
                        order.getUserId(),
                        "Оплата наличными при получении подтверждена"
                );

                insertTransaction(
                        conn,
                        paymentId,
                        order.getId(),
                        order.getUserId(),
                        "PAYMENT",
                        "SUCCESS",
                        order.getTotalPrice(),
                        "CASH_ON_DELIVERY",
                        transactionId,
                        "Платёж подтверждён без передачи платёжных данных в приложение"
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

    public boolean completeGatewayPayment(int paymentId,
                                          int userId,
                                          String gatewayTransactionId,
                                          String gatewayStatus,
                                          String message) {

        String paymentSql = """
            SELECT *
            FROM payments
            WHERE id=?
              AND user_id=?
            FOR UPDATE
        """;

        String updatePaymentSql = """
            UPDATE payments
            SET status=?, gateway_transaction_id=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Payment payment;

                try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                    stmt.setInt(1, paymentId);
                    stmt.setInt(2, userId);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    payment = mapPayment(rs);
                }

                if ("SUCCESS".equals(payment.getStatus())) {
                    conn.commit();
                    return true;
                }

                if (!"PENDING".equals(payment.getStatus())) {
                    conn.rollback();
                    return false;
                }

                boolean success = "SUCCESS".equals(gatewayStatus);
                String newPaymentStatus = success ? "SUCCESS" : "FAILED";

                try (PreparedStatement stmt = conn.prepareStatement(updatePaymentSql)) {
                    stmt.setString(1, newPaymentStatus);
                    stmt.setString(2, gatewayTransactionId);
                    stmt.setInt(3, payment.getId());
                    stmt.executeUpdate();
                }

                if (success) {
                    String oldStatus = lockOrderAndGetStatus(conn, payment.getOrderId());

                    if (oldStatus == null ||
                            "CANCELLED".equals(oldStatus) ||
                            "COMPLETED".equals(oldStatus)) {

                        conn.rollback();
                        return false;
                    }

                    updateOrderStatus(conn, payment.getOrderId(), "PAID");

                    insertOrderStatusHistory(
                            conn,
                            payment.getOrderId(),
                            oldStatus,
                            "PAID",
                            userId,
                            "Платёж подтверждён платёжным шлюзом"
                    );
                }

                insertTransaction(
                        conn,
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getUserId(),
                        "PAYMENT",
                        newPaymentStatus,
                        payment.getAmount(),
                        payment.getProvider(),
                        gatewayTransactionId,
                        message
                );

                conn.commit();
                return success;

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

        String paymentSql = """
            SELECT *
            FROM payments
            WHERE order_id=?
              AND status='SUCCESS'
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            FOR UPDATE
        """;

        String refundPaymentSql = """
            UPDATE payments
            SET
                status='REFUNDED',
                refund_reason=?,
                refunded_at=CURRENT_TIMESTAMP
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Payment payment;

                try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                    stmt.setInt(1, orderId);

                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    payment = mapPayment(rs);
                }

                String oldStatus = lockOrderAndGetStatus(conn, orderId);

                if (oldStatus == null ||
                        "COMPLETED".equals(oldStatus) ||
                        "CANCELLED".equals(oldStatus)) {

                    conn.rollback();
                    return false;
                }

                try (PreparedStatement stmt = conn.prepareStatement(refundPaymentSql)) {
                    stmt.setString(1, reason);
                    stmt.setInt(2, payment.getId());
                    stmt.executeUpdate();
                }

                updateOrderStatus(conn, orderId, "CANCELLED");

                insertOrderStatusHistory(
                        conn,
                        orderId,
                        oldStatus,
                        "CANCELLED",
                        null,
                        "Возврат платежа: " + reason
                );

                insertTransaction(
                        conn,
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getUserId(),
                        "REFUND",
                        "REFUNDED",
                        payment.getAmount(),
                        payment.getProvider(),
                        payment.getGatewayTransactionId(),
                        reason
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

    private String lockOrderAndGetStatus(Connection conn,
                                         int orderId) throws SQLException {

        String sql = """
            SELECT status
            FROM orders
            WHERE id=?
            FOR UPDATE
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
        }

        return null;
    }

    private void updateOrderStatus(Connection conn,
                                   int orderId,
                                   String status) throws SQLException {

        String sql = """
            UPDATE orders
            SET status=?
            WHERE id=?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
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

    private void insertTransaction(Connection conn,
                                   int paymentId,
                                   int orderId,
                                   int userId,
                                   String transactionType,
                                   String status,
                                   java.math.BigDecimal amount,
                                   String provider,
                                   String gatewayTransactionId,
                                   String message) throws SQLException {

        String sql = """
            INSERT INTO payment_transactions(
                payment_id,
                order_id,
                user_id,
                transaction_type,
                status,
                amount,
                provider,
                gateway_transaction_id,
                message
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            stmt.setInt(2, orderId);
            stmt.setInt(3, userId);
            stmt.setString(4, transactionType);
            stmt.setString(5, status);
            stmt.setBigDecimal(6, amount);
            stmt.setString(7, provider);
            stmt.setString(8, gatewayTransactionId);
            stmt.setString(9, message);
            stmt.executeUpdate();
        }
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();

        payment.setId(rs.getInt("id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setStatus(rs.getString("status"));
        payment.setCreatedAt(rs.getTimestamp("created_at"));
        payment.setRefundReason(rs.getString("refund_reason"));
        payment.setRefundedAt(rs.getTimestamp("refunded_at"));

        try {
            payment.setProvider(rs.getString("provider"));
            payment.setGatewayTransactionId(rs.getString("gateway_transaction_id"));
        } catch (Exception ignored) {
        }

        return payment;
    }

    private PaymentTransaction mapTransaction(ResultSet rs)
            throws SQLException {

        PaymentTransaction transaction = new PaymentTransaction();

        transaction.setId(rs.getInt("id"));
        transaction.setPaymentId(rs.getInt("payment_id"));
        transaction.setOrderId(rs.getInt("order_id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setStatus(rs.getString("status"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setProvider(rs.getString("provider"));
        transaction.setGatewayTransactionId(rs.getString("gateway_transaction_id"));
        transaction.setMessage(rs.getString("message"));
        transaction.setCreatedAt(rs.getTimestamp("created_at"));

        return transaction;
    }
}
