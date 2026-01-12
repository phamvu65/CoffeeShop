package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // [SỬA] Tên bảng nên là số nhiều để tránh trùng keyword SQL
    private static final String TABLE_NAME = "user";

    public User checkLogin(String username, String password) {
        User user = null;
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public List<User> getAllActiveUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // [QUAN TRỌNG] Map đầy đủ các cột mới (Email, Birthday, HourlyRate)
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .fullname(rs.getString("fullname"))
                .phone(rs.getString("phone"))
                .email(rs.getString("email"))           // [SỬA] Đọc email
                .birthday(rs.getString("birthday"))     // [SỬA] Đọc ngày sinh
                .gender(rs.getBoolean("gender"))
                .hourlyRate(rs.getBigDecimal("hourly_rate"))
                .roleId(rs.getInt("role_id"))
                .isActive(rs.getBoolean("is_active"))
                .build();
    }

    // [QUAN TRỌNG] Thêm mới: Phải Insert cả EMAIL và BIRTHDAY
    public void addUser(User u) {
        // [SỬA] Bổ sung email, birthday vào câu lệnh INSERT
        String sql = "INSERT INTO " + TABLE_NAME + " (fullname, username, password, phone, email, birthday, gender, hourly_rate, role_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getFullname());
            stmt.setString(2, u.getUsername());
            stmt.setString(3, u.getPassword());
            stmt.setString(4, u.getPhone());
            stmt.setString(5, u.getEmail());       // [SỬA] Lưu Email
            stmt.setString(6, u.getBirthday());    // [SỬA] Lưu Ngày sinh

            // Xử lý null cho gender (tránh lỗi NullPointerException)
            stmt.setBoolean(7, u.getGender() != null && u.getGender());

            stmt.setBigDecimal(8, u.getHourlyRate());
            stmt.setInt(9, u.getRoleId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // [QUAN TRỌNG] Cập nhật: Cũng phải Update cả EMAIL và BIRTHDAY
    public void updateUser(User u) {
        boolean updatePass = u.getPassword() != null && !u.getPassword().isEmpty();

        // [SỬA] Bổ sung email=?, birthday=?
        String sql = "UPDATE " + TABLE_NAME + " SET fullname=?, phone=?, email=?, birthday=?, gender=?, hourly_rate=?, username=?, role_id=?"
                + (updatePass ? ", password=?" : "")
                + " WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getFullname());
            stmt.setString(2, u.getPhone());
            stmt.setString(3, u.getEmail());        // [SỬA] Update Email
            stmt.setString(4, u.getBirthday());     // [SỬA] Update Ngày sinh
            stmt.setBoolean(5, u.getGender() != null && u.getGender());
            stmt.setBigDecimal(6, u.getHourlyRate());
            stmt.setString(7, u.getUsername());
            stmt.setInt(8, u.getRoleId());

            int paramIndex = 9;
            if (updatePass) {
                stmt.setString(paramIndex++, u.getPassword());
            }
            stmt.setInt(paramIndex, u.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(int id) {
        String sql = "UPDATE " + TABLE_NAME + " SET is_active = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE username = ? AND is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean checkEmailExists(String email) {
        // [LƯU Ý] Hàm này dùng cho chức năng Quên mật khẩu
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updatePasswordByEmail(String email, String newPassword) {
        String sql = "UPDATE " + TABLE_NAME + " SET password = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}