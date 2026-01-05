package com.vuxnye.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Receipt {
    private int id;
    private double totalPrice;
    private String paymentMethod; // "TIỀN MẶT" hoặc "CHUYỂN KHOẢN"
    private int customerId;       // ID khách hàng (có thể là 0 nếu khách vãng lai)
    private Timestamp createdAt;  // Thời gian tạo đơn
    private String customerName;

    public Receipt(int id, double totalPrice, String paymentMethod, int customerId, Timestamp createdAt) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public String getFormattedDate() {
        if (createdAt == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(createdAt);
    }
}