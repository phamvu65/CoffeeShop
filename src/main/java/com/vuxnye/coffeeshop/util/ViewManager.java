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

    // Bộ nhớ đệm lưu trữ các view đã load (Key: đường dẫn FXML, Value: giao diện)
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

    public void switchView(String fxmlPath) {
        if (mainBorderPane == null) {
            System.err.println("❌ Lỗi: MainBorderPane chưa được khởi tạo!");
            return;
        }

        try {
            Parent viewNode;

            // Kiểm tra xem view này đã có trong cache chưa
            if (viewCache.containsKey(fxmlPath)) {
                // Nếu có rồi -> Lấy ra dùng lại ngay (Siêu nhanh)
                viewNode = viewCache.get(fxmlPath);
            } else {
                // Nếu chưa có -> Load từ file FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                viewNode = loader.load();

                // Lưu vào cache để lần sau dùng lại
                viewCache.put(fxmlPath, viewNode);
            }

            // Hiển thị lên màn hình chính
            mainBorderPane.setCenter(viewNode);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi: Không tìm thấy file FXML tại: " + fxmlPath);
        }
    }

    /**
     * [QUAN TRỌNG] Phương thức bạn đang thiếu
     * Xóa sạch bộ nhớ đệm khi người dùng đăng xuất.
     */
    public void clearCache() {
        viewCache.clear();
        System.out.println("♻️ Đã xóa cache giao diện.");
    }
}