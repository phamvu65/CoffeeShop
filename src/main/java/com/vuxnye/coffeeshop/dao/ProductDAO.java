package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.Product;
import com.vuxnye.coffeeshop.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String TABLE_NAME = "product";

    // =========================================================================
    // PHẦN 1: TÌM KIẾM & LỌC (SEARCH)
    // =========================================================================

    /**
     * Tìm kiếm sản phẩm nâng cao
     * @param keyword: Tên món ăn
     * @param categoryId: ID danh mục (0 = Tất cả)
     * @param onlyActive: true = Chỉ lấy món đang bán (POS), false = Lấy tất cả (Admin)
     */
    public List<Product> searchProducts(String keyword, int categoryId, boolean onlyActive) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as cat_name FROM " + TABLE_NAME + " p " +
                        "LEFT JOIN category c ON p.category_id = c.id " +
                        "WHERE p.name LIKE ? "
        );

        if (onlyActive) {
            sql.append("AND p.is_active = 1 ");
        }

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
                list.add(mapRow(rs)); // Sử dụng hàm mapRow cho gọn
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Product> getAllProducts() {
        return searchProducts("", 0, false);
    }

    // =========================================================================
    // [BỔ SUNG] PHẦN 2: PHÂN TRANG (PAGINATION)
    // =========================================================================

    /**
     * Lấy danh sách sản phẩm theo trang (Có JOIN để lấy tên danh mục)
     */
    public List<Product> getProductsPaging(int limit, int offset, int categoryId) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as cat_name FROM product p " +
                        "LEFT JOIN category c ON p.category_id = c.id " +
                        "WHERE 1=1 " // Mẹo để dễ nối chuỗi AND
        );

        // Nếu có chọn danh mục
        if (categoryId > 0) {
            sql.append("AND p.category_id = ? ");
        }

        sql.append("ORDER BY p.id DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (categoryId > 0) {
                stmt.setInt(paramIndex++, categoryId);
            }
            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs)); // Dùng hàm mapRow ở bài trước
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Đếm tổng số lượng món ăn (Để tính tổng số trang)
     */
    public int countTotalProducts(int categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM product WHERE 1=1 ");

        if (categoryId > 0) {
            sql.append("AND category_id = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (categoryId > 0) {
                stmt.setInt(1, categoryId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // =========================================================================
    // PHẦN 3: CRUD (THÊM, SỬA, XÓA) - [ĐÃ BỔ SUNG unit & description]
    // =========================================================================

    public void addProduct(Product p) {
        // [BỔ SUNG] Thêm unit và description vào câu INSERT
        String sql = "INSERT INTO " + TABLE_NAME + " (name, category_id, price, unit, description, image_path, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName());
            stmt.setInt(2, p.getCategoryId());
            stmt.setDouble(3, p.getPrice());
            stmt.setString(4, p.getUnit());        // Bổ sung
            stmt.setString(5, p.getDescription()); // Bổ sung
            stmt.setString(6, p.getImagePath());

            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateProduct(Product p) {
        // [BỔ SUNG] Thêm unit và description vào câu UPDATE
        String sql = "UPDATE " + TABLE_NAME + " SET name=?, category_id=?, price=?, unit=?, description=?, image_path=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName());
            stmt.setInt(2, p.getCategoryId());
            stmt.setDouble(3, p.getPrice());
            stmt.setString(4, p.getUnit());        // Bổ sung
            stmt.setString(5, p.getDescription()); // Bổ sung
            stmt.setString(6, p.getImagePath());
            stmt.setInt(7, p.getId());

            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteProduct(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void toggleStatus(int id, boolean isActive) {
        String sql = "UPDATE " + TABLE_NAME + " SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================================
    // [BỔ SUNG] PHẦN 4: HELPER METHOD (Tránh lặp code)
    // =========================================================================

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setPrice(rs.getDouble("price"));
        p.setUnit(rs.getString("unit"));
        p.setImagePath(rs.getString("image_path"));
        p.setDescription(rs.getString("description"));
        p.setActive(rs.getBoolean("is_active"));

        // Kiểm tra xem câu SQL có join bảng category không để lấy tên
        try {
            p.setCategoryName(rs.getString("cat_name"));
        } catch (SQLException e) {
            // Nếu câu query không join (ví dụ select đơn giản), bỏ qua lỗi này
            p.setCategoryName("N/A");
        }

        return p;
    }
}