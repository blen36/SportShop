package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();

        String sql = "SELECT * FROM categories ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        (Integer) rs.getObject("parent_id")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}