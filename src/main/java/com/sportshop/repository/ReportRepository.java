package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.DiscountReportRow;
import com.sportshop.models.PopularProductReportRow;
import com.sportshop.models.SalesReportRow;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    public List<SalesReportRow> getSalesByDay(int limit) {

        List<SalesReportRow> rows = new ArrayList<>();

        String sql = """
            SELECT
                TO_CHAR(created_at::date, 'YYYY-MM-DD') AS period,
                COUNT(*) AS orders_count,
                COALESCE(SUM(total_price), 0) AS revenue
            FROM orders
            WHERE status != 'CANCELLED'
            GROUP BY created_at::date
            ORDER BY created_at::date DESC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rows.add(new SalesReportRow(
                        rs.getString("period"),
                        rs.getInt("orders_count"),
                        rs.getBigDecimal("revenue")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    public List<PopularProductReportRow> getPopularProducts(int limit) {

        List<PopularProductReportRow> rows = new ArrayList<>();

        String sql = """
            SELECT
                p.id AS product_id,
                p.name AS product_name,
                COALESCE(SUM(oi.quantity), 0) AS sold_quantity,
                COALESCE(SUM(oi.quantity * oi.price), 0) AS revenue
            FROM order_items oi
            JOIN products p
                ON oi.product_id = p.id
            JOIN orders o
                ON oi.order_id = o.id
            WHERE o.status != 'CANCELLED'
            GROUP BY p.id, p.name
            ORDER BY sold_quantity DESC, revenue DESC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rows.add(new PopularProductReportRow(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("sold_quantity"),
                        rs.getBigDecimal("revenue")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    public List<DiscountReportRow> getDiscountEffectiveness() {

        List<DiscountReportRow> rows = new ArrayList<>();

        String sql = """
            SELECT
                o.promo_code,
                COALESCE(d.name, 'Промокод') AS discount_name,
                COUNT(*) AS orders_count,
                COALESCE(SUM(o.discount_amount), 0) AS total_discount
            FROM orders o
            LEFT JOIN discounts d
                ON UPPER(d.code) = UPPER(o.promo_code)
            WHERE o.promo_code IS NOT NULL
              AND o.discount_amount > 0
              AND o.status != 'CANCELLED'
            GROUP BY o.promo_code, d.name
            ORDER BY total_discount DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rows.add(new DiscountReportRow(
                        rs.getString("promo_code"),
                        rs.getString("discount_name"),
                        rs.getInt("orders_count"),
                        rs.getBigDecimal("total_discount")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    public BigDecimal getRefundedAmount() {

        String sql = """
            SELECT COALESCE(SUM(amount), 0)
            FROM payments
            WHERE status = 'REFUNDED'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }
}