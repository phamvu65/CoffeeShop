package com.vuxnye.coffeeshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Table {
    private int id;
    private String name;
    private String status; // 'EMPTY', 'SERVING', 'RESERVED'
}