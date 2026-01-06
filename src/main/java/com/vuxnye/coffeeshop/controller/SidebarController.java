package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.Refreshable; // [QUAN TRỌNG] Import Interface
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
    @FXML private Button btnStaff;
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

        // 2. Phân quyền
        if (user != null && user.getRoleId() != RoleEnum.ADMIN.getId()) {
            // Nếu là STAFF -> Ẩn các chức năng quản lý
            hideButton(btnDashboard);
            hideButton(btnReports);
            hideButton(btnStaff); // Staff thường không quản lý nhân viên

            // Mặc định vào POS
            handlePOS();
        } else {
            // Admin mặc định vào Dashboard
            handleDashboard();
        }
    }

    // =========================================================================
    // [MỚI] HÀM ĐIỀU HƯỚNG CHUNG (NAVIGATE)
    // =========================================================================
    // Hàm này giúp code gọn hơn và tự động Refresh dữ liệu
    private void navigate(String fxmlPath, Button activeBtn) {
        // 1. Đổi màu nút active
        setActiveStyle(activeBtn);

        // 2. Chuyển cảnh và nhận về Controller
        Object controller = ViewManager.getInstance().switchView(fxmlPath);

        // 3. Nếu Controller đó có chức năng Refreshable -> Gọi refreshData()
        if (controller instanceof Refreshable) {
            ((Refreshable) controller).refreshData();
        }
    }

    // --- Navigation Handlers (Đã được viết lại gọn gàng) ---

    @FXML
    public void handleDashboard() {
        navigate("/com/vuxnye/coffeeshop/view/Dashboard.fxml", btnDashboard);
    }

    @FXML
    public void handlePOS() {
        navigate("/com/vuxnye/coffeeshop/view/POS.fxml", btnPOS);
    }

    @FXML
    public void handleTable() {
        navigate("/com/vuxnye/coffeeshop/view/TableView.fxml", btnTable);
    }

    @FXML
    public void handleMenu() {
        navigate("/com/vuxnye/coffeeshop/view/Menu.fxml", btnMenu);
    }

    @FXML
    public void handleCustomers() {
        navigate("/com/vuxnye/coffeeshop/view/Customers.fxml", btnCustomers);
    }

    @FXML
    public void handleStaff() {
        navigate("/com/vuxnye/coffeeshop/view/Staff.fxml", btnStaff);
    }

    @FXML
    public void handlePromotion() {
        navigate("/com/vuxnye/coffeeshop/view/Promotions.fxml", btnPromotion);
    }

    @FXML
    public void handleReports() {
        navigate("/com/vuxnye/coffeeshop/view/Reports.fxml", btnReports);
    }

    @FXML
    public void handleLogout() {
        try {
            // Reset cache view
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
        resetStyle(btnDashboard);
        resetStyle(btnPOS);
        resetStyle(btnTable);
        resetStyle(btnMenu);
        resetStyle(btnCustomers);
        resetStyle(btnStaff);
        resetStyle(btnPromotion);
        resetStyle(btnReports);

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
            btn.setManaged(false);
        }
    }
}