package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {
    @FXML private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        // Đăng ký pane chính để quản lý chuyển cảnh
        ViewManager.getInstance().setMainBorderPane(mainBorderPane);

        // Mặc định mở màn hình POS khi vào
        ViewManager.getInstance().switchView("/com/vuxnye/coffeeshop/view/POS.fxml");


    }
}