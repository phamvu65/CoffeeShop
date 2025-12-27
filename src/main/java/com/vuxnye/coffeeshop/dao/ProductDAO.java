package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.Product;
import com.vuxnye.coffeeshop.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as cat_name FROM product p " +
                "LEFT JOIN category c ON p.category_id = c.id " +
                "WHERE p.is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setPrice(rs.getDouble("price"));
                p.setUnit(rs.getString("unit"));
                p.setImagePath(rs.getString("image_path")); // Map đúng cột image_path
                p.setDescription(rs.getString("description"));
                p.setActive(rs.getBoolean("is_active"));
                p.setCategoryName(rs.getString("cat_name"));

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Product> searchProducts(String keyword, int categoryId) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as cat_name FROM product p " +
                        "LEFT JOIN category c ON p.category_id = c.id " +
                        "WHERE p.is_active = 1 AND p.name LIKE ? "
        );

        // Nếu có chọn danh mục (id > 0) thì thêm điều kiện
        if (categoryId > 0) {
            sql.append("AND p.category_id = ? ");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, "%" + keyword + "%");

            if (categoryId > 0) {
                stmt.setInt(2, categoryId);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setPrice(rs.getDouble("price"));
                p.setUnit(rs.getString("unit"));
                p.setImagePath(rs.getString("image_path"));
                p.setDescription(rs.getString("description"));
                p.setActive(rs.getBoolean("is_active"));
                p.setCategoryName(rs.getString("cat_name"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}