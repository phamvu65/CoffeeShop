package com.vuxnye.coffeeshop.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppUtils {
    private static final String ICON_PATH = "/images/cup_2935413.png";

    public static void setAppIcon(Stage stage) {
        try {
            // Cách này gọn, gọi ở đâu cũng được
            stage.getIcons().add(new Image(AppUtils.class.getResourceAsStream(ICON_PATH)));
        } catch (Exception e) {
            System.err.println("Không tìm thấy icon tại: " + ICON_PATH);
        }
    }
}