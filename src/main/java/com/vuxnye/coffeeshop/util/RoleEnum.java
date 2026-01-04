package com.vuxnye.coffeeshop.util;

public enum RoleEnum {
    ADMIN(1, "Quản lý"),
    STAFF(2, "Thu ngân");

    private final int id;
    private final String label;

    RoleEnum(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }

    // Hàm tiện ích: Lấy Enum từ ID trong DB
    public static RoleEnum fromId(int id) {
        for (RoleEnum r : values()) {
            if (r.id == id) return r;
        }
        return STAFF; // Mặc định là nhân viên
    }
}