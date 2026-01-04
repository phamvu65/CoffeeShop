package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.UserDAO;
import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.EmailService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtForgotEmail;

    @FXML private HBox boxUsername;
    @FXML private HBox boxPassword;
    @FXML private HBox boxEmail;

    @FXML private VBox loginForm;
    @FXML private VBox forgotForm;

    @FXML
    public void initialize() {
        forgotForm.setVisible(false);
        loginForm.setVisible(true);

        setupFocusEffect(txtUsername, boxUsername);
        setupFocusEffect(txtPassword, boxPassword);
        setupFocusEffect(txtForgotEmail, boxEmail);

        Platform.runLater(() -> loginForm.requestFocus());

        Platform.runLater(() -> {
            if (loginForm.getScene() != null) {
                loginForm.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.F5) reloadView();
                });
            }
        });
    }

    // --- XỬ LÝ ĐĂNG NHẬP ---
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập tên đăng nhập và mật khẩu!");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.checkLogin(username, password);

        if (user != null) {
            // --- CHUYỂN SANG MÀN HÌNH CHÍNH (MAIN LAYOUT) ---
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/MainLayout.fxml"));
                Parent root = loader.load();

                // [QUAN TRỌNG] Lấy Controller và truyền User Session vào
                MainLayoutController mainController = loader.getController();
                mainController.setLoggedInUser(user);
                // Hàm này sẽ tự động setup Sidebar (ẩn nút) và load Dashboard/POS

                // --- TẠO STAGE MỚI ĐỂ FULL MÀN HÌNH ---

                // 1. Tạo một cửa sổ (Stage) hoàn toàn mới
                Stage mainStage = new Stage();
                Scene scene = new Scene(root);

                // Nếu muốn add CSS toàn cục
                // scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                mainStage.setScene(scene);
                mainStage.setTitle("BaristaFlow - Hệ thống quản lý quán cà phê");

                // 2. Phóng to cửa sổ mới này
                mainStage.setMaximized(true);

                // 3. Hiển thị cửa sổ chính
                mainStage.show();

                // 4. Đóng cửa sổ Login cũ đi
                Stage loginStage = (Stage) loginForm.getScene().getWindow();
                loginStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện chính: " + e.getMessage());
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
    }


    // --- XỬ LÝ QUÊN MẬT KHẨU (GIỮ NGUYÊN) ---
    @FXML
    public void handleSendRequest(ActionEvent event) {
        String email = txtForgotEmail.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Email!");
            return;
        }

        UserDAO userDAO = new UserDAO();
        if (!userDAO.checkEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email này không tồn tại trong hệ thống!");
            return;
        }

        String newPassword = generateRandomPassword(6);
        boolean isUpdated = userDAO.updatePasswordByEmail(email, newPassword);

        if (isUpdated) {
            new Thread(() -> {
                try {
                    String subject = "BaristaFlow - Mật khẩu mới";
                    String body = "<h2>BaristaFlow System</h2>"
                            + "<p>Mật khẩu mới của bạn là: <b style='color:red; font-size:18px'>" + newPassword + "</b></p>";

                    EmailService.sendEmail(email, subject, body);

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi mật khẩu mới vào email: " + email);
                        showLoginForm(null);
                        txtPassword.setText("");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi gửi mail", e.getMessage()));
                }
            }).start();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể cập nhật mật khẩu.");
        }
    }

    @FXML
    public void showForgotPassword(ActionEvent event) { switchView(loginForm, forgotForm); }

    @FXML
    public void showLoginForm(ActionEvent event) { switchView(forgotForm, loginForm); }

    private void switchView(VBox hideView, VBox showView) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), hideView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            hideView.setVisible(false);
            showView.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), showView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void setupFocusEffect(TextField field, HBox box) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) box.getStyleClass().add("input-group-focused");
            else box.getStyleClass().remove("input-group-focused");
        });
    }

    private String generateRandomPassword(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void reloadView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginForm.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) { e.printStackTrace(); }
    }
}