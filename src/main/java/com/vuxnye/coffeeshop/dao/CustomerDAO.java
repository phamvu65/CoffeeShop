package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.Customer;
import com.vuxnye.coffeeshop.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // Lấy danh sách khách hàng (Có tìm kiếm)
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE name LIKE ? OR phone LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getInt("points")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // [QUAN TRỌNG] Tìm khách theo SĐT (Dùng cho Checkout)
    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM customer WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getInt("points")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Thêm khách hàng mới
    public void addCustomer(Customer c) {
        String sql = "INSERT INTO customer (name, phone, points) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getName());
            stmt.setString(2, c.getPhone());
            stmt.setInt(3, c.getPoints());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Cập nhật thông tin khách
    public void updateCustomer(Customer c) {
        String sql = "UPDATE customer SET name=?, phone=?, points=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getName());
            stmt.setString(2, c.getPhone());
            stmt.setInt(3, c.getPoints());
            stmt.setInt(4, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // [QUAN TRỌNG] Cộng điểm tích lũy
    public void addPoints(int customerId, int pointsToAdd) {
        String sql = "UPDATE customer SET points = points + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pointsToAdd);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Xóa khách hàng
    public void deleteCustomer(int id) {
        String sql = "DELETE FROM customer WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Customer> getAllCustomers() {
        return searchCustomers("");
    }
}