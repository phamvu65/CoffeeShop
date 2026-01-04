package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.util.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReportDAO {

    // 1. Thống kê doanh thu theo Danh mục (Dùng cho PieChart)
    // Logic: Kết nối bảng chi tiết -> sản phẩm -> danh mục
    public Map<String, Double> getRevenueByCategory() {
        Map<String, Double> data = new HashMap<>();
        String sql = "SELECT c.name AS category_name, SUM(d.subtotal) as total " +
                "FROM receipt_detail d " +
                "JOIN product p ON d.product_id = p.id " +
                "JOIN category c ON p.category_id = c.id " +
                "GROUP BY c.name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.put(rs.getString("category_name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // 2. Top 5 sản phẩm bán chạy nhất (Dùng cho BarChart)
    // Logic: Tính tổng số lượng bán theo sản phẩm
    public Map<String, Integer> getTopSellingProducts() {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT p.name AS product_name, SUM(d.quantity) as total_sold " +
                "FROM receipt_detail d " +
                "JOIN product p ON d.product_id = p.id " +
                "GROUP BY p.id, p.name " +
                "ORDER BY total_sold DESC " +
                "LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.put(rs.getString("product_name"), rs.getInt("total_sold"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // 3. Tổng doanh thu toàn hệ thống (Dùng cho thẻ thống kê)
    public double getTotalRevenue() {
        String sql = "SELECT SUM(subtotal) FROM receipt_detail";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}