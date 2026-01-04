package com.vuxnye.coffeeshop.model;

import com.vuxnye.coffeeshop.util.RoleEnum;
import lombok.*;
import java.math.BigDecimal; // [QUAN TRỌNG] Import class này

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private String username;
    private String password;
    private String fullname;

    // [SỬA LỖI] Dùng Boolean (Wapper) thay vì boolean để Lombok sinh hàm getGender()
    private Boolean gender;

    private String phone;
    private String email;
    private String birthday; // Có thể nâng cấp lên LocalDate sau này

    // [SỬA LỖI] Dùng BigDecimal thay vì double để xử lý tiền tệ và check null
    private BigDecimal hourlyRate;

    private int roleId;
    private boolean isActive;

    // Helper method để lấy tên hiển thị của Role
    public String getRoleName() {
        return RoleEnum.fromId(this.roleId).getLabel();
    }
}