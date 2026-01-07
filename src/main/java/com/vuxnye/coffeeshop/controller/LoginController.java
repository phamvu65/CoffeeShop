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
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image; // [QUAN TRỌNG] Đã thêm import này
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

    @FXML private Button btnLogin;
    @FXML private Button btnForgot;

    private final UserDAO userDAO = new UserDAO();

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

    // --- 1. XỬ LÝ ĐĂNG NHẬP ---
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập tên đăng nhập và mật khẩu!");
            return;
        }

        setLoadingState(btnLogin, true, "Đang kết nối...");

        new Thread(() -> {
            User user = userDAO.checkLogin(username, password);

            Platform.runLater(() -> {
                if (user != null) {
                    openMainLayout(user);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Tên đăng nhập hoặc mật khẩu không chính xác.");
                    setLoadingState(btnLogin, false, "BẮT ĐẦU LÀM VIỆC");
                }
            });
        }).start();
    }

    private void openMainLayout(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainLayout.fxml"));
            Parent root = loader.load();

            MainLayoutController mainController = loader.getController();
            mainController.setLoggedInUser(user);

            // --- TẠO STAGE MỚI ---
            Stage mainStage = new Stage();

            // [FIX] THÊM ICON CHO CỬA SỔ CHÍNH TẠI ĐÂY
            try {
                // Đảm bảo file ảnh nằm đúng trong thư mục: src/main/resources/images/
                Image icon = new Image(getClass().getResourceAsStream("/images/cup_2935413.png"));
                mainStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Lỗi không tìm thấy icon: " + e.getMessage());
            }
            // ---------------------------------------------

            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.setTitle("BaristaFlow - Hệ thống quản lý quán cà phê");
            mainStage.setMaximized(true);
            mainStage.show();

            Stage loginStage = (Stage) loginForm.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện chính: " + e.getMessage());
            setLoadingState(btnLogin, false, "BẮT ĐẦU LÀM VIỆC");
        }
    }


    // --- 2. XỬ LÝ QUÊN MẬT KHẨU ---
    @FXML
    public void handleSendRequest(ActionEvent event) {
        String email = txtForgotEmail.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Email!");
            return;
        }

        setLoadingState(btnForgot, true, "Đang kiểm tra...");

        new Thread(() -> {
            boolean exists = userDAO.checkEmailExists(email);

            if (!exists) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Email này không tồn tại trong hệ thống!");
                    setLoadingState(btnForgot, false, "GỬI YÊU CẦU");
                });
                return;
            }

            Platform.runLater(() -> btnForgot.setText("Đang gửi mail..."));

            String newPassword = generateRandomPassword(6);
            boolean isUpdated = userDAO.updatePasswordByEmail(email, newPassword);

            if (isUpdated) {
                try {
                    String subject = "BaristaFlow - Khôi phục mật khẩu";
                    String body = "<h2>BaristaFlow System</h2>"
                            + "<p>Mật khẩu mới của bạn là: <b style='color:blue; font-size:18px'>" + newPassword + "</b></p>"
                            + "<p>Vui lòng đăng nhập và đổi mật khẩu ngay.</p>";

                    EmailService.sendEmail(email, subject, body);

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu mới đã được gửi tới: " + email);
                        showLoginForm(null);
                        txtPassword.setText("");
                        setLoadingState(btnForgot, false, "GỬI YÊU CẦU");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Lỗi gửi mail", e.getMessage());
                        setLoadingState(btnForgot, false, "GỬI YÊU CẦU");
                    });
                }
            } else {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể cập nhật mật khẩu Database.");
                    setLoadingState(btnForgot, false, "GỬI YÊU CẦU");
                });
            }
        }).start();
    }

    // --- UTILITIES ---
    private void setLoadingState(Button btn, boolean isLoading, String text) {
        if (btn == null) return;
        btn.setDisable(isLoading);
        btn.setText(text);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginForm.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}