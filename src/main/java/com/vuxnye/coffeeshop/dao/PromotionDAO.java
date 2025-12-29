package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.Promotion;
import com.vuxnye.coffeeshop.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {

    public List<Promotion> getAllPromotions() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Promotion(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getDouble("value"),
                        rs.getDouble("min_order_value"),
                        rs.getDouble("max_discount"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Promotion> searchPromotions(String keyword) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion WHERE code LIKE ? OR description LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Promotion(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getDouble("value"),
                        rs.getDouble("min_order_value"),
                        rs.getDouble("max_discount"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addPromotion(Promotion p) {
        String sql = "INSERT INTO promotion (code, description, type, value, min_order_value, max_discount, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getCode());
            stmt.setString(2, p.getDescription());
            stmt.setString(3, p.getType());
            stmt.setDouble(4, p.getValue());
            stmt.setDouble(5, p.getMinOrderValue());
            stmt.setDouble(6, p.getMaxDiscount());
            stmt.setDate(7, p.getStartDate());
            stmt.setDate(8, p.getEndDate());
            stmt.setBoolean(9, p.isActive());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePromotion(Promotion p) {
        String sql = "UPDATE promotion SET code=?, description=?, type=?, value=?, min_order_value=?, max_discount=?, start_date=?, end_date=?, is_active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getCode());
            stmt.setString(2, p.getDescription());
            stmt.setString(3, p.getType());
            stmt.setDouble(4, p.getValue());
            stmt.setDouble(5, p.getMinOrderValue());
            stmt.setDouble(6, p.getMaxDiscount());
            stmt.setDate(7, p.getStartDate());
            stmt.setDate(8, p.getEndDate());
            stmt.setBoolean(9, p.isActive());
            stmt.setInt(10, p.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deletePromotion(int id) {
        String sql = "DELETE FROM promotion WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void toggleStatus(int id, boolean isActive) {
        String sql = "UPDATE promotion SET is_active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Promotion getPromotionByCode(String code) {
        String sql = "SELECT * FROM promotion WHERE code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Promotion(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getString("type"),
                        rs.getDouble("value"),
                        rs.getDouble("min_order_value"),
                        rs.getDouble("max_discount"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getBoolean("is_active")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}