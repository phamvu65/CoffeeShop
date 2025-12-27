package com.vuxnye.coffeeshop.dao;

import com.vuxnye.coffeeshop.model.CartItem;
import com.vuxnye.coffeeshop.model.Customer;
import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.DatabaseConnection;

import java.sql.*;
import java.util.List;

public class ReceiptDAO {

    public boolean createReceipt(List<CartItem> cartItems, double totalAmount, double discount, double finalAmount,
                                 String paymentMethod, Customer customer, User staff) {

        Connection conn = null;
        PreparedStatement stmtReceipt = null;
        PreparedStatement stmtDetail = null;
        PreparedStatement stmtUpdatePoint = null;
        ResultSet rs = null;

        // SQL Insert vào bảng 'receipt'
        // status mặc định là 'PAID' vì POS bán tại quầy là thu tiền luôn
        String sqlReceipt = "INSERT INTO receipt (customer_id, employee_id, total_amount, discount_amount, final_amount, payment_method, status) VALUES (?, ?, ?, ?, ?, ?, 'PAID')";

        // SQL Insert vào bảng 'receipt_detail'
        String sqlDetail = "INSERT INTO receipt_detail (receipt_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

        String sqlUpdatePoint = "UPDATE customer SET points = ? WHERE id = ?";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // --- BƯỚC 1: TẠO HÓA ĐƠN (RECEIPT) ---
            stmtReceipt = conn.prepareStatement(sqlReceipt, Statement.RETURN_GENERATED_KEYS);

            // Set Customer ID
            if (customer != null) stmtReceipt.setInt(1, customer.getId());
            else stmtReceipt.setNull(1, java.sql.Types.INTEGER);

            // Set Employee ID (staff)
            if (staff != null) stmtReceipt.setInt(2, staff.getId());
            else stmtReceipt.setNull(2, java.sql.Types.INTEGER);

            stmtReceipt.setDouble(3, totalAmount);
            stmtReceipt.setDouble(4, discount);
            stmtReceipt.setDouble(5, finalAmount);
            stmtReceipt.setString(6, paymentMethod); // 'CASH' hoặc 'BANK_TRANSFER' (Map từ code cũ)

            int affectedRows = stmtReceipt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Tạo hóa đơn thất bại.");

            // Lấy ID hóa đơn vừa tạo
            int receiptId = 0;
            rs = stmtReceipt.getGeneratedKeys();
            if (rs.next()) {
                receiptId = rs.getInt(1);
            } else {
                throw new SQLException("Không lấy được ID hóa đơn.");
            }

            // --- BƯỚC 2: TẠO CHI TIẾT HÓA ĐƠN (RECEIPT DETAIL) ---
            stmtDetail = conn.prepareStatement(sqlDetail);
            for (CartItem item : cartItems) {
                stmtDetail.setInt(1, receiptId);
                stmtDetail.setInt(2, item.getProduct().getId());
                stmtDetail.setInt(3, item.getQuantity());
                stmtDetail.setDouble(4, item.getProduct().getPrice()); // unit_price
                stmtDetail.setDouble(5, item.getTotal()); // subtotal = price * quantity
                stmtDetail.addBatch();
            }
            stmtDetail.executeBatch();

            // --- BƯỚC 3: CẬP NHẬT ĐIỂM (Nếu có khách) ---
            if (customer != null) {
                int pointsUsed = (int) (discount / 1000);
                int pointsEarned = (int) (finalAmount / 10000); // 10k = 1 điểm
                int newPoints = customer.getPoints() - pointsUsed + pointsEarned;
                if (newPoints < 0) newPoints = 0;

                stmtUpdatePoint = conn.prepareStatement(sqlUpdatePoint);
                stmtUpdatePoint.setInt(1, newPoints);
                stmtUpdatePoint.setInt(2, customer.getId());
                stmtUpdatePoint.executeUpdate();
            }

            conn.commit(); // Lưu tất cả
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmtReceipt != null) stmtReceipt.close();
                if (stmtDetail != null) stmtDetail.close();
                if (stmtUpdatePoint != null) stmtUpdatePoint.close();
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}