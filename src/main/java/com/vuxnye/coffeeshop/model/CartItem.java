package com.vuxnye.coffeeshop.model;

import lombok.Data;

@Data
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public String getName() {
        return product.getName();
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }
}