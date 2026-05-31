package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM categories
            ORDER BY parent_id NULLS FIRST, name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapCategory(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Category findById(int id) {
        String sql = """
            SELECT *
            FROM categories
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapCategory(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public int save(Category category) {
        if (category.getId() > 0) {
            update(category);
            return category.getId();
        }

        String sql = """
            INSERT INTO categories(name, parent_id)
            VALUES (?, ?)
            RETURNING id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillStatement(stmt, category);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean update(Category category) {
        String sql = """
            UPDATE categories
            SET name=?, parent_id=?
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillStatement(stmt, category);
            stmt.setInt(3, category.getId());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(int id) {
        String sql = """
            DELETE FROM categories
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasChildCategories(int id) {
        String sql = """
            SELECT COUNT(*)
            FROM categories
            WHERE parent_id=?
        """;

        return countById(sql, id) > 0;
    }

    public boolean hasProducts(int id) {
        String sql = """
            SELECT COUNT(*)
            FROM products
            WHERE category_id=?
        """;

        return countById(sql, id) > 0;
    }

    private int countById(String sql, int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void fillStatement(PreparedStatement stmt,
                               Category category) throws SQLException {

        stmt.setString(1, category.getName());

        if (category.getParentId() == null) {
            stmt.setNull(2, Types.INTEGER);
        } else {
            stmt.setInt(2, category.getParentId());
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                (Integer) rs.getObject("parent_id")
        );
    }
}
