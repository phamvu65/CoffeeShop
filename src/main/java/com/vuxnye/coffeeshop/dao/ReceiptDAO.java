package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.CartItem;
import com.vuxnye.coffeeshop.model.Receipt;
import com.vuxnye.coffeeshop.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDAO {

    // 1. Lấy tất cả hóa đơn (Dùng cho Dashboard/Lịch sử)
    public List<Receipt> getAllReceipts() {
        List<Receipt> list = new ArrayList<>();
        String sql = "SELECT * FROM receipt ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Receipt(
                        rs.getInt("id"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_method"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Tạo hóa đơn chính (Trả về ID để dùng cho bước lưu chi tiết)
    public int createReceipt(Receipt receipt) {
        String sql = "INSERT INTO receipt (total_amount, payment_method, customer_id, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDouble(1, receipt.getTotalAmount());
            stmt.setString(2, receipt.getPaymentMethod());

            if (receipt.getCustomerId() > 0) {
                stmt.setInt(3, receipt.getCustomerId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Trả về ID hóa đơn vừa tạo
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Trả về -1 nếu lỗi
    }

    // 3. [QUAN TRỌNG] Lưu chi tiết hóa đơn (Để ReportDAO có dữ liệu vẽ biểu đồ)
    public void createReceiptDetails(int receiptId, List<CartItem> items) {
        String sql = "INSERT INTO receipt_detail (receipt_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (CartItem item : items) {
                stmt.setInt(1, receiptId);
                stmt.setInt(2, item.getProduct().getId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getProduct().getPrice());
                stmt.setDouble(5, item.getTotal());

                stmt.addBatch(); // Gom lệnh lại chạy 1 lần cho tối ưu
            }

            stmt.executeBatch(); // Thực thi lưu tất cả

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}