package com.vuxnye.coffeeshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

//    private static final String URL = "jdbc:mysql://localhost:3306/coffee_management";
//    private static final String USERNAME = "root";
//    private static final String PASSWORD = "1234";
//
//    public static Connection getConnection() {
//        Connection conn = null;
//        try{
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//            System.err.println("Lỗi kết nối CSDL!");
//        }
//        return conn;
//    }

    private static final String HOST = "bd0viwzjlfypjonlssla-mysql.services.clever-cloud.com"; // [HOST]
    private static final String DB_NAME = "bd0viwzjlfypjonlssla"; // [DB NAME]
    private static final String USER = "u3amhqx96ospprok";    // [USER]
    private static final String PASS = "046xS2wCHHDsuDVitckg";    // [PASSWORD]

    // Chuỗi kết nối JDBC chuẩn
    private static final String URL = "jdbc:mysql://" + HOST + ":3306/" + DB_NAME;

    public static Connection getConnection() {
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver"); // Giữ dòng này nếu cần
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi kết nối Cloud Database!");
            return null;
        }
    }

}
