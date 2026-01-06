package com.vuxnye.coffeeshop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewManager {

    private static ViewManager instance;
    private BorderPane mainBorderPane;

    // Bộ nhớ đệm lưu trữ các view đã load
    private final Map<String, Parent> viewCache = new HashMap<>();

    private ViewManager() {}

    public static synchronized ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    public void setMainBorderPane(BorderPane pane) {
        this.mainBorderPane = pane;
    }

    /**
     * Chuyển đổi giao diện và trả về Controller tương ứng
     * @param fxmlPath Đường dẫn file FXML
     * @return Object (Controller của view đó) hoặc null nếu lỗi
     */
    public Object switchView(String fxmlPath) {
        if (mainBorderPane == null) {
            System.err.println("❌ Lỗi: MainBorderPane chưa được khởi tạo!");
            return null;
        }

        try {
            Parent viewNode;
            Object controller;

            // 1. Kiểm tra cache
            if (viewCache.containsKey(fxmlPath)) {
                // Lấy view từ cache
                viewNode = viewCache.get(fxmlPath);

                // [MẸO] Lấy controller đã được gắn vào view từ lần load trước
                controller = viewNode.getUserData();
            } else {
                // 2. Load mới từ FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                viewNode = loader.load();
                controller = loader.getController(); // Lấy controller mới

                // [MẸO] Gắn controller vào view để lần sau lấy lại được
                if (controller != null) {
                    viewNode.setUserData(controller);
                }

                // Lưu vào cache
                viewCache.put(fxmlPath, viewNode);
            }

            // 3. Hiển thị lên màn hình
            mainBorderPane.setCenter(viewNode);

            // 4. Trả về controller cho SidebarController sử dụng (để refresh data)
            return controller;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi: Không tìm thấy file FXML tại: " + fxmlPath);
            return null;
        }
    }

    public void clearCache() {
        viewCache.clear();
        System.out.println("♻️ Đã xóa cache giao diện.");
    }
}