package com.vuxnye.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private int id;
    private String name;
    private int categoryId;
    private double price;
    private String unit;
    private String imagePath;
    private String description;
    private boolean isActive;
    private String categoryName;
}