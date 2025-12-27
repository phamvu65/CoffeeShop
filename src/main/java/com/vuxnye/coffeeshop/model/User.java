package com.vuxnye.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User {
    private int id;
    private String username;
    private String password;
    private String fullname;
    private String phone;
    private String birthday;
    private String email;
    private boolean gender;
    private double salary;
    private int roleId;
    private boolean isActive;
}
