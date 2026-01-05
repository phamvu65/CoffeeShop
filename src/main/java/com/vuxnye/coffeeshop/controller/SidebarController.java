package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.RoleEnum;
import com.vuxnye.coffeeshop.util.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class SidebarController {

    // --- FXML UI Components ---
    @FXML private Button btnDashboard;
    @FXML private Button btnPOS;
    @FXML private Button btnTable;
    @FXML private Button btnMenu;
    @FXML private Button btnCustomers;
    @FXML private Button btnStaff;     // [CÓ] Nút Quản lý nhân viên
    @FXML private Button btnPromotion;
    @FXML private Button btnReports;
    @FXML private Label lblUser;

    // --- Data ---
    private User currentUser;

    @FXML
    public void initialize() {
        // Khởi tạo mặc định
    }

    /**
     * Nhận user từ Login/MainLayout và phân quyền hiển thị
     */
    public void setLoggedInUser(User user) {
        this.currentUser = user;

        // 1. Hiển thị tên
        if (lblUser != null && user != null) {
            lblUser.setText("Hi, " + user.getUsername());
        }

        // 2. Phân quyền (Role-based Access Control)
        // Nếu KHÔNG PHẢI ADMIN (là Staff) -> Ẩn các chức năng quản lý
        if (user != null && user.getRoleId() != RoleEnum.ADMIN.getId()) {
            hideButton(btnDashboard);
//            hideButton(btnMenu);
//            hideButton(btnStaff);      // [QUAN TRỌNG] Nhân viên không được xem danh sách nhân viên
//            hideButton(btnCustomers);
//            hideButton(btnPromotion);
            hideButton(btnReports);

            // Mặc định vào POS
            handlePOS();
        } else {
            // Admin mặc định vào Dashboard
            handleDashboard();
        }
    }

    // --- Navigation Handlers ---

    @FXML
    public void handleDashboard() {
        setActiveStyle(btnDashboard);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Dashboard.fxml");
    }

    @FXML
    public void handlePOS() {
        setActiveStyle(btnPOS);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/POS.fxml");
    }

    @FXML
    public void handleTable() {
        setActiveStyle(btnTable);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/TableView.fxml");
    }

    @FXML
    public void handleMenu() {
        setActiveStyle(btnMenu);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Menu.fxml");
    }

    @FXML
    public void handleCustomers() {
        setActiveStyle(btnCustomers);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Customers.fxml");
    }

    // [CÓ] Thêm xử lý cho nút nhân viên
    @FXML
    public void handleStaff() {
        setActiveStyle(btnStaff);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Staff.fxml");
    }

    @FXML
    public void handlePromotion() {
        setActiveStyle(btnPromotion);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Promotions.fxml");
    }

    @FXML
    public void handleReports() {
        setActiveStyle(btnReports);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Reports.fxml");
    }

    @FXML
    public void handleLogout() {
        try {
            // Reset cache view để lần sau đăng nhập không bị lưu trạng thái cũ
            ViewManager.getInstance().clearCache();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/Login.fxml"));
            Stage stage = (Stage) lblUser.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Helper Methods ---

    private void setActiveStyle(Button activeBtn) {
        // Reset style tất cả các nút
        resetStyle(btnDashboard);
        resetStyle(btnPOS);
        resetStyle(btnTable);
        resetStyle(btnMenu);
        resetStyle(btnCustomers);
        resetStyle(btnStaff);      // [QUAN TRỌNG] Reset cả nút Staff
        resetStyle(btnPromotion);
        resetStyle(btnReports);

        // Set style active cho nút được chọn
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("menu-btn-active");
        }
    }

    private void resetStyle(Button btn) {
        if (btn != null) {
            btn.getStyleClass().remove("menu-btn-active");
        }
    }

    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false); // Xóa khỏi layout flow để không chiếm chỗ trống
        }
    }
}