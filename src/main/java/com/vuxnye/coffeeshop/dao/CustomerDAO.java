package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.Customer;
import com.vuxnye.coffeeshop.util.DatabaseConnection;
import java.sql.*;

public class CustomerDAO {

    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customer WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("phone"), rs.getInt("points")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void createCustomer(Customer c) {
        // Cột created_at sẽ tự động sinh, không cần insert
        String sql = "INSERT INTO customer (name, phone, points) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getName());
            stmt.setString(2, c.getPhone());
            stmt.setInt(3, 0); // Điểm khởi tạo = 0
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}