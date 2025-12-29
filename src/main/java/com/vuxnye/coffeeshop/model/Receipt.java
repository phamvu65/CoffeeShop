package com.vuxnye.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Receipt {
    private int id;
    private double totalAmount;
    private String paymentMethod; // "TIỀN MẶT" hoặc "CHUYỂN KHOẢN"
    private int customerId;       // ID khách hàng (có thể là 0 nếu khách vãng lai)
    private Timestamp createdAt;  // Thời gian tạo đơn
}