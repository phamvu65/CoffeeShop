package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.util.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class SidebarController {

    @FXML private Button btnDashboard;
    @FXML private Button btnPOS;
    @FXML private Button btnTable;
    @FXML private Button btnMenu;
    @FXML private Button btnPromotion;
    @FXML private Label lblUser;

    @FXML
    public void initialize() {
        // Mặc định có thể set active cho Dashboard khi mới vào
        // setActiveStyle(btnDashboard);
    }

    // [MỚI] Xử lý nút Tổng quan
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
    public void handlePromotion() {
        setActiveStyle(btnPromotion);
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/Promotions.fxml");
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/Login.fxml"));
            Stage stage = (Stage) lblUser.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void setActiveStyle(Button activeBtn) {
        // Reset style tất cả các nút
        resetStyle(btnDashboard);
        resetStyle(btnPOS);
        resetStyle(btnTable);
        resetStyle(btnMenu);
        resetStyle(btnPromotion);

        // Set style active cho nút được chọn
        activeBtn.getStyleClass().add("menu-btn-active");
    }

    private void resetStyle(Button btn) {
        if(btn != null) {
            btn.getStyleClass().remove("menu-btn-active");
        }
    }
}