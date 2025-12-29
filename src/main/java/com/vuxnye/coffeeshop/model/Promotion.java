package com.vuxnye.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data @AllArgsConstructor @NoArgsConstructor
public class Promotion {
    private int id;
    private String code;
    private String description; // Tên chương trình (name trong React code)
    private String type; // 'PERCENT' hoặc 'FIXED'
    private double value;
    private double minOrderValue;
    private double maxDiscount;
    private Date startDate;
    private Date endDate;
    private boolean isActive;

    // Helper để hiển thị trên bảng
    public String getTypeName() {
        return "PERCENT".equals(type) ? "Phần trăm (%)" : "Số tiền cố định (đ)";
    }
}