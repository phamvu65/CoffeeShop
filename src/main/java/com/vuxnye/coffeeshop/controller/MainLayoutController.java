package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.RoleEnum;
import com.vuxnye.coffeeshop.util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {

    @FXML
    private BorderPane mainBorderPane;

    // QUAN TRỌNG: Tên biến phải khớp với fx:id="sidebar" trong MainLayout.fxml + "Controller"
    // Ví dụ: <fx:include fx:id="sidebar" source="Sidebar.fxml"/>
    @FXML
    private SidebarController sidebarController;

    @FXML
    public void initialize() {
        // Đăng ký pane chính để quản lý chuyển cảnh
        ViewManager.getInstance().setMainBorderPane(mainBorderPane);

        // Lưu ý: Không nên switchView cứng ở đây nữa,
        // hãy để hàm setLoggedInUser quyết định dựa trên Role.
    }

    /**
     * Hàm này được gọi từ LoginController sau khi đăng nhập thành công
     */
    public void setLoggedInUser(User user) {
        // 1. Truyền user xuống Sidebar để ẩn/hiện nút
        if (sidebarController != null) {
            sidebarController.setLoggedInUser(user);
        }

        // 2. Mở màn hình mặc định dựa trên quyền
        // Nếu sidebarController đã xử lý logic này rồi thì có thể bỏ qua,
        // nhưng khai báo ở đây cho chắc chắn.
        if (user.getRoleId() == RoleEnum.ADMIN.getId()) {
            ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Dashboard.fxml");
        } else {
            ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/POS.fxml");
        }
    }
}