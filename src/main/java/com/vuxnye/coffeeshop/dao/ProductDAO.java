package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.Product;
import com.vuxnye.coffeeshop.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    /**
     * Tìm kiếm sản phẩm nâng cao
     * @param keyword: Tên món ăn
     * @param categoryId: ID danh mục (0 = Tất cả)
     * @param onlyActive: true = Chỉ lấy món đang bán (POS), false = Lấy tất cả (Admin)
     */
    public List<Product> searchProducts(String keyword, int categoryId, boolean onlyActive) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as cat_name FROM product p " +
                        "LEFT JOIN category c ON p.category_id = c.id " +
                        "WHERE p.name LIKE ? "
        );

        // Nếu là POS (onlyActive = true) -> Chỉ hiện món is_active = 1
        if (onlyActive) {
            sql.append("AND p.is_active = 1 ");
        }

        // Lọc theo danh mục
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

    // Hàm tương thích ngược (mặc định lấy tất cả - dùng cho trang quản lý)
    public List<Product> searchProducts(String keyword, int categoryId) {
        return searchProducts(keyword, categoryId, false);
    }

    // Hàm lấy tất cả (dùng cho debug hoặc load all)
    public List<Product> getAllProducts() {
        return searchProducts("", 0, false);
    }

    // --- CÁC HÀM CRUD (THÊM, SỬA, XÓA, ẨN) ---

    public void addProduct(Product p) {
        String sql = "INSERT INTO product (name, category_id, price, image_path, is_active) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setInt(2, p.getCategoryId());
            stmt.setDouble(3, p.getPrice());
            stmt.setString(4, p.getImagePath());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateProduct(Product p) {
        String sql = "UPDATE product SET name=?, category_id=?, price=?, image_path=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setInt(2, p.getCategoryId());
            stmt.setDouble(3, p.getPrice());
            stmt.setString(4, p.getImagePath());
            stmt.setInt(5, p.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteProduct(int id) {
        String sql = "DELETE FROM product WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void toggleStatus(int id, boolean isActive) {
        String sql = "UPDATE product SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}