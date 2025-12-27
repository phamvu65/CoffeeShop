package com.vuxnye.coffeeshop.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class Category {
    private int id;
    private String name;
    @Override
    public String toString() {
        return name;
    }
}