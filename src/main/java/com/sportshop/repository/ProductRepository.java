package com.sportshop.repository;

import com.sportshop.config.DBConnection;
import com.sportshop.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private static final String PRODUCT_SELECT = """
        SELECT
            p.id,
            p.name,
            p.description,
            p.price,
            p.brand,
            p.category_id,
            COALESCE(p.attributes, '{}'::jsonb)::text AS attributes,

            COALESCE(i.quantity, 0) AS stock,

            (
                SELECT pi.image_url
                FROM product_images pi
                WHERE pi.product_id = p.id
                ORDER BY pi.id
                LIMIT 1
            ) AS image_url,

            COALESCE(
                (
                    SELECT AVG(r.rating)
                    FROM reviews r
                    WHERE r.product_id = p.id
                      AND r.status = 'APPROVED'
                ),
                0
            ) AS average_rating,

            (
                SELECT COUNT(*)
                FROM reviews r
                WHERE r.product_id = p.id
                  AND r.status = 'APPROVED'
            ) AS reviews_count

        FROM products p
        LEFT JOIN inventory i
            ON p.id = i.product_id
    """;

    public List<Product> findAll() {
        return searchWithFilters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1000,
                0
        );
    }

    public int countAll() {
        return countWithFilters(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Product findById(int id) {

        String sql = PRODUCT_SELECT + """
            WHERE p.id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapProduct(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Product> findProductsByIds(List<Integer> ids) {

        List<Product> products = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            return products;
        }

        for (Integer id : ids) {

            if (id == null) {
                continue;
            }

            Product product = findById(id);

            if (product != null) {
                products.add(product);
            }
        }

        return products;
    }

    public List<Product> searchWithFilters(String query,
                                           String brand,
                                           Double minPrice,
                                           Double maxPrice,
                                           Integer categoryId,
                                           Double minRating,
                                           String sort,
                                           int limit,
                                           int offset) {

        List<Product> products = new ArrayList<>();

        StringBuilder sql = new StringBuilder(PRODUCT_SELECT);
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        appendFilters(
                sql,
                params,
                query,
                brand,
                minPrice,
                maxPrice,
                categoryId,
                minRating
        );

        appendSort(sql, sort);

        sql.append(" LIMIT ? OFFSET ? ");

        params.add(limit);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            setParams(stmt, params);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public int countWithFilters(String query,
                                String brand,
                                Double minPrice,
                                Double maxPrice,
                                Integer categoryId,
                                Double minRating) {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM products p
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        appendFilters(
                sql,
                params,
                query,
                brand,
                minPrice,
                maxPrice,
                categoryId,
                minRating
        );

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            setParams(stmt, params);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<Product> findRelatedProducts(Product product,
                                             int limit) {

        List<Product> products = new ArrayList<>();

        if (product == null || product.getCategoryId() == null) {
            return findPopularProducts(limit);
        }

        String sql = PRODUCT_SELECT + """
            WHERE p.id <> ?
              AND p.category_id = ?
            ORDER BY
                COALESCE(
                    (
                        SELECT AVG(r.rating)
                        FROM reviews r
                        WHERE r.product_id = p.id
                          AND r.status = 'APPROVED'
                    ),
                    0
                ) DESC,
                p.id DESC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product.getId());
            stmt.setInt(2, product.getCategoryId());
            stmt.setInt(3, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (products.isEmpty()) {
            return findPopularProducts(limit);
        }

        return products;
    }

    public List<Product> findPopularProducts(int limit) {

        List<Product> products = new ArrayList<>();

        String sql = PRODUCT_SELECT + """
            WHERE 1=1
            ORDER BY
                (
                    SELECT COALESCE(SUM(oi.quantity), 0)
                    FROM order_items oi
                    WHERE oi.product_id = p.id
                ) DESC,
                (
                    SELECT COUNT(*)
                    FROM view_history vh
                    WHERE vh.product_id = p.id
                ) DESC,
                p.id DESC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> findRecommendedProducts(int userId,
                                                 int limit) {

        List<Product> products = new ArrayList<>();

        String sql = """
            WITH user_categories AS (
                SELECT DISTINCT p.category_id
                FROM view_history vh
                JOIN products p
                    ON vh.product_id = p.id
                WHERE vh.user_id = ?
                  AND p.category_id IS NOT NULL

                UNION

                SELECT DISTINCT p.category_id
                FROM orders o
                JOIN order_items oi
                    ON o.id = oi.order_id
                JOIN products p
                    ON oi.product_id = p.id
                WHERE o.user_id = ?
                  AND p.category_id IS NOT NULL
            )
        """ + PRODUCT_SELECT + """
            WHERE p.category_id IN (
                SELECT category_id
                FROM user_categories
            )
            AND p.id NOT IN (
                SELECT oi.product_id
                FROM orders o
                JOIN order_items oi
                    ON o.id = oi.order_id
                WHERE o.user_id = ?
            )
            ORDER BY
                (
                    SELECT COUNT(*)
                    FROM view_history vh
                    WHERE vh.product_id = p.id
                ) DESC,
                (
                    SELECT COALESCE(SUM(oi.quantity), 0)
                    FROM order_items oi
                    WHERE oi.product_id = p.id
                ) DESC,
                p.id DESC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (products.isEmpty()) {
            return findPopularProducts(limit);
        }

        return products;
    }

    public List<Product> findRecentlyViewedProducts(int userId,
                                                    int limit) {

        List<Integer> ids = new ArrayList<>();

        String sql = """
            SELECT product_id
            FROM view_history
            WHERE user_id = ?
            GROUP BY product_id
            ORDER BY MAX(viewed_at) DESC
            LIMIT ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("product_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return findProductsByIds(ids);
    }

    public int create(Product product) {

        String sql = """
            INSERT INTO products(
                name,
                description,
                price,
                brand,
                category_id,
                attributes
            )
            VALUES (?, ?, ?, ?, ?, ?::jsonb)
            RETURNING id
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, product.getName());
                stmt.setString(2, product.getDescription());
                stmt.setBigDecimal(3, product.getPrice());
                stmt.setString(4, product.getBrand());
                stmt.setObject(5, product.getCategoryId());
                stmt.setString(6, normalizeAttributes(product.getAttributes()));

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    int productId = rs.getInt("id");

                    saveMainImage(
                            conn,
                            productId,
                            product.getImageUrl()
                    );

                    conn.commit();

                    return productId;
                }

                conn.rollback();

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void update(Product product) {

        String sql = """
            UPDATE products
            SET
                name=?,
                description=?,
                price=?,
                brand=?,
                category_id=?,
                attributes=?::jsonb
            WHERE id=?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, product.getName());
                stmt.setString(2, product.getDescription());
                stmt.setBigDecimal(3, product.getPrice());
                stmt.setString(4, product.getBrand());
                stmt.setObject(5, product.getCategoryId());
                stmt.setString(6, normalizeAttributes(product.getAttributes()));
                stmt.setInt(7, product.getId());

                stmt.executeUpdate();

                saveMainImage(
                        conn,
                        product.getId(),
                        product.getImageUrl()
                );

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {

        String sql = "DELETE FROM products WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendFilters(StringBuilder sql,
                               List<Object> params,
                               String query,
                               String brand,
                               Double minPrice,
                               Double maxPrice,
                               Integer categoryId,
                               Double minRating) {

        if (query != null && !query.isBlank()) {

            sql.append("""
                AND (
                    p.name ILIKE ?
                    OR COALESCE(p.description, '') ILIKE ?
                    OR COALESCE(p.attributes::text, '') ILIKE ?
                )
            """);

            String q = "%" + query.trim() + "%";

            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (brand != null && !brand.isBlank()) {

            sql.append("""
                AND COALESCE(p.brand, '') ILIKE ?
            """);

            params.add("%" + brand.trim() + "%");
        }

        if (minPrice != null) {

            sql.append("""
                AND p.price >= ?
            """);

            params.add(minPrice);
        }

        if (maxPrice != null) {

            sql.append("""
                AND p.price <= ?
            """);

            params.add(maxPrice);
        }

        if (categoryId != null) {

            sql.append("""
                AND p.category_id IN (
                    WITH RECURSIVE subcategories AS (
                        SELECT id
                        FROM categories
                        WHERE id = ?

                        UNION ALL

                        SELECT c.id
                        FROM categories c
                        JOIN subcategories sc
                            ON c.parent_id = sc.id
                    )
                    SELECT id
                    FROM subcategories
                )
            """);

            params.add(categoryId);
        }

        if (minRating != null) {

            sql.append("""
                AND COALESCE(
                    (
                        SELECT AVG(r.rating)
                        FROM reviews r
                        WHERE r.product_id = p.id
                          AND r.status = 'APPROVED'
                    ),
                    0
                ) >= ?
            """);

            params.add(minRating);
        }
    }

    private void appendSort(StringBuilder sql,
                            String sort) {

        if ("price_asc".equals(sort)) {

            sql.append(" ORDER BY p.price ASC ");

        } else if ("price_desc".equals(sort)) {

            sql.append(" ORDER BY p.price DESC ");

        } else if ("name".equals(sort)) {

            sql.append(" ORDER BY p.name ASC ");

        } else if ("rating_desc".equals(sort)) {

            sql.append("""
                ORDER BY
                    COALESCE(
                        (
                            SELECT AVG(r.rating)
                            FROM reviews r
                            WHERE r.product_id = p.id
                              AND r.status = 'APPROVED'
                        ),
                        0
                    ) DESC
            """);

        } else if ("stock_desc".equals(sort)) {

            sql.append(" ORDER BY COALESCE(i.quantity, 0) DESC ");

        } else {

            sql.append(" ORDER BY p.id DESC ");
        }
    }

    private void setParams(PreparedStatement stmt,
                           List<Object> params) throws SQLException {

        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    private void saveMainImage(Connection conn,
                               int productId,
                               String imageUrl) throws SQLException {

        String deleteSql = """
            DELETE FROM product_images
            WHERE product_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String insertSql = """
            INSERT INTO product_images(product_id, image_url)
            VALUES (?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setInt(1, productId);
            stmt.setString(2, imageUrl.trim());

            stmt.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs)
            throws SQLException {

        Product p = new Product();

        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setBrand(rs.getString("brand"));
        p.setCategoryId((Integer) rs.getObject("category_id"));
        p.setAttributes(rs.getString("attributes"));
        p.setImageUrl(rs.getString("image_url"));
        p.setStock(rs.getInt("stock"));
        p.setAverageRating(rs.getDouble("average_rating"));
        p.setReviewsCount(rs.getInt("reviews_count"));

        return p;
    }

    private String normalizeAttributes(String attributes) {

        if (attributes == null || attributes.isBlank()) {
            return "{}";
        }

        String value = attributes.trim();

        if (value.startsWith("{") && value.endsWith("}")) {
            return value;
        }

        String[] parts = value.split("[;\\n]");
        StringBuilder json = new StringBuilder("{");

        boolean hasPairs = false;

        for (String part : parts) {

            String line = part.trim();

            if (line.isEmpty()) {
                continue;
            }

            int separatorIndex = line.indexOf(":");

            if (separatorIndex < 0) {
                separatorIndex = line.indexOf("=");
            }

            if (separatorIndex <= 0) {
                continue;
            }

            String key = line.substring(0, separatorIndex).trim();
            String val = line.substring(separatorIndex + 1).trim();

            if (key.isEmpty() || val.isEmpty()) {
                continue;
            }

            if (hasPairs) {
                json.append(",");
            }

            json.append("\"")
                    .append(escapeJson(key))
                    .append("\":\"")
                    .append(escapeJson(val))
                    .append("\"");

            hasPairs = true;
        }

        if (!hasPairs) {
            return "{\"Описание\":\"" + escapeJson(value) + "\"}";
        }

        json.append("}");

        return json.toString();
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}