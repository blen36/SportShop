package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Discount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountRepository {

    public List<Discount> findAll() {

        List<Discount> discounts = new ArrayList<>();

        String sql = """
            SELECT *
            FROM discounts
            ORDER BY id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                discounts.add(mapDiscount(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return discounts;
    }

    public Discount findById(int id) {

        String sql = """
            SELECT *
            FROM discounts
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapDiscount(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Discount findActivePromoByCode(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        String sql = """
            SELECT *
            FROM discounts
            WHERE UPPER(code) = UPPER(?)
              AND active = TRUE
              AND (start_date IS NULL OR CURRENT_TIMESTAMP >= start_date)
              AND (end_date IS NULL OR CURRENT_TIMESTAMP <= end_date)
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.trim());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapDiscount(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public int save(Discount discount) {

        if (discount.getId() > 0) {
            update(discount);
            return discount.getId();
        }

        String sql = """
            INSERT INTO discounts(
                name,
                type,
                value,
                start_date,
                end_date,
                code,
                active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillDiscountStatement(stmt, discount);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void update(Discount discount) {

        String sql = """
            UPDATE discounts
            SET
                name=?,
                type=?,
                value=?,
                start_date=?,
                end_date=?,
                code=?,
                active=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillDiscountStatement(stmt, discount);
            stmt.setInt(8, discount.getId());

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {

        String sql = """
            DELETE FROM discounts
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceProducts(int discountId,
                                List<Integer> productIds) {

        String deleteSql = """
            DELETE FROM product_discounts
            WHERE discount_id=?
        """;

        String insertSql = """
            INSERT INTO product_discounts(
                product_id,
                discount_id
            )
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement stmt =
                             conn.prepareStatement(deleteSql)) {

                    stmt.setInt(1, discountId);
                    stmt.executeUpdate();
                }

                if (productIds != null && !productIds.isEmpty()) {

                    try (PreparedStatement stmt =
                                 conn.prepareStatement(insertSql)) {

                        for (Integer productId : productIds) {

                            if (productId == null || productId <= 0) {
                                continue;
                            }

                            stmt.setInt(1, productId);
                            stmt.setInt(2, discountId);
                            stmt.addBatch();
                        }

                        stmt.executeBatch();
                    }
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

    public List<Integer> findProductIdsByDiscountId(int discountId) {

        List<Integer> ids = new ArrayList<>();

        String sql = """
            SELECT product_id
            FROM product_discounts
            WHERE discount_id=?
            ORDER BY product_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, discountId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("product_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    private void fillDiscountStatement(PreparedStatement stmt,
                                       Discount discount)
            throws SQLException {

        stmt.setString(1, discount.getName());
        stmt.setString(2, discount.getType());
        stmt.setBigDecimal(3, discount.getValue());

        if (discount.getStartDate() == null) {
            stmt.setNull(4, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(4, discount.getStartDate());
        }

        if (discount.getEndDate() == null) {
            stmt.setNull(5, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(5, discount.getEndDate());
        }

        if (discount.getCode() == null ||
                discount.getCode().isBlank()) {

            stmt.setNull(6, Types.VARCHAR);

        } else {

            stmt.setString(
                    6,
                    discount.getCode().trim().toUpperCase()
            );
        }

        stmt.setBoolean(7, discount.isActive());
    }

    private Discount mapDiscount(ResultSet rs)
            throws SQLException {

        Discount discount = new Discount();

        discount.setId(rs.getInt("id"));
        discount.setName(rs.getString("name"));
        discount.setType(rs.getString("type"));
        discount.setValue(rs.getBigDecimal("value"));
        discount.setStartDate(rs.getTimestamp("start_date"));
        discount.setEndDate(rs.getTimestamp("end_date"));
        discount.setCode(rs.getString("code"));
        discount.setActive(rs.getBoolean("active"));

        return discount;
    }
}