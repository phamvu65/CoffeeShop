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


    public Product(int id, String name, int categoryId, String categoryName, double price, String imagePath, boolean isActive) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.imagePath = imagePath;
        this.isActive = isActive;

        this.unit = "";
        this.description = "";
    }
}