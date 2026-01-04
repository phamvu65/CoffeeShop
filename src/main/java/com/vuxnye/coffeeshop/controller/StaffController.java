package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.UserDAO;
import com.vuxnye.coffeeshop.model.User;
import com.vuxnye.coffeeshop.util.RoleEnum;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class StaffController {

    // --- FXML UI Components ---
    @FXML private TextField txtSearch;
    @FXML private TableView<User> tableStaff;

    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colGender;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colHourlyRate; // Đã đổi tên biến cho rõ nghĩa
    @FXML private TableColumn<User, Void> colAction;

    // --- Data Management ---
    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @FXML
    public void initialize() {
        setupColumns();
        setupSearch();
        loadData();
    }

    private void setupColumns() {
        // 1. Các cột String cơ bản
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullname()));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        // 2. Cột Giới tính (Boolean -> String)
        colGender.setCellValueFactory(data -> {
            Boolean gender = data.getValue().getGender();
            String text = (gender != null && gender) ? "Nam" : "Nữ";
            return new SimpleStringProperty(text);
        });

        // 3. Cột Lương/Giờ (Format VNĐ + "/h")
        colHourlyRate.setCellValueFactory(data -> {
            BigDecimal rate = data.getValue().getHourlyRate(); // Lấy lương theo giờ
            if (rate == null) rate = BigDecimal.ZERO;

            // Format: 25.000 đ
            String formatted = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(rate);

            // Thêm hậu tố /h
            return new SimpleStringProperty(formatted + "/h");
        });

        // 4. Cột Vai trò (Badge màu)
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoleName()));
        colRole.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 10;");

                    User u = getTableView().getItems().get(getIndex());
                    if (u.getRoleId() == RoleEnum.ADMIN.getId()) {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #F3E8FF; -fx-text-fill: #7E22CE;"); // Tím
                    } else {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;"); // Xanh
                    }
                    setGraphic(lbl); setText(null);
                }
            }
        });

        // 5. Cột Thao tác
        colAction.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");
                btnDelete.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");

                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(user -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();

                return user.getFullname().toLowerCase().contains(lower)
                        || user.getUsername().toLowerCase().contains(lower)
                        || (user.getPhone() != null && user.getPhone().contains(lower));
            });
        });

        tableStaff.setItems(filteredData);
    }

    private void loadData() {
        masterData.setAll(userDAO.getAllActiveUsers());
    }

    // --- BUTTON HANDLERS ---

    @FXML
    private void handleAddNew() {
        showStaffDialog(null);
    }

    private void handleEdit(User user) {
        showStaffDialog(user);
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa nhân viên " + user.getFullname() + "?");
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userDAO.deleteUser(user.getId());
            loadData();
        }
    }

    // --- MODAL DIALOG ---

    private void showStaffDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Thêm nhân viên" : "Cập nhật nhân viên");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        // UI Controls
        TextField txtFullname = new TextField(user != null ? user.getFullname() : "");
        TextField txtPhone = new TextField(user != null ? user.getPhone() : "");

        ComboBox<String> cbGender = new ComboBox<>();
        cbGender.getItems().addAll("Nam", "Nữ");
        cbGender.setValue(user != null && Boolean.TRUE.equals(user.getGender()) ? "Nam" : "Nữ");

        // Load Hourly Rate
        TextField txtRate = new TextField(user != null && user.getHourlyRate() != null
                ? String.valueOf(user.getHourlyRate().longValue())
                : "25000"); // Mặc định 25k/h

        TextField txtUsername = new TextField(user != null ? user.getUsername() : "");
        if (user != null) txtUsername.setDisable(true);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(user != null ? "Để trống nếu không đổi" : "Mật khẩu...");

        ComboBox<RoleEnum> cbRole = new ComboBox<>();
        cbRole.getItems().addAll(RoleEnum.values());
        cbRole.setValue(user != null ? RoleEnum.fromId(user.getRoleId()) : RoleEnum.STAFF);

        // Grid Layout
        grid.add(new Label("Họ tên (*):"), 0, 0);   grid.add(txtFullname, 1, 0);
        grid.add(new Label("SĐT:"), 0, 1);          grid.add(txtPhone, 1, 1);
        grid.add(new Label("Giới tính:"), 0, 2);    grid.add(cbGender, 1, 2);

        // Label mới
        grid.add(new Label("Lương/Giờ (VNĐ):"), 0, 3); grid.add(txtRate, 1, 3);

        grid.add(new Label("Username (*):"), 0, 4); grid.add(txtUsername, 1, 4);
        grid.add(new Label("Mật khẩu:"), 0, 5);     grid.add(txtPassword, 1, 5);
        grid.add(new Label("Chức vụ:"), 0, 6);      grid.add(cbRole, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Convert Result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveType) {
                if (txtFullname.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()) {
                    return null;
                }

                // Xử lý Lương: Xóa ký tự lạ, chỉ giữ số
                BigDecimal rateVal;
                try {
                    String cleanRate = txtRate.getText().trim().replaceAll("[^\\d]", "");
                    rateVal = cleanRate.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanRate);
                } catch (Exception e) {
                    rateVal = BigDecimal.ZERO;
                }

                return User.builder()
                        .id(user != null ? user.getId() : 0)
                        .fullname(txtFullname.getText().trim())
                        .phone(txtPhone.getText().trim())
                        .gender(cbGender.getValue().equals("Nam"))
                        .hourlyRate(rateVal) // Sử dụng field hourlyRate
                        .username(txtUsername.getText().trim())
                        .password(txtPassword.getText())
                        .roleId(cbRole.getValue().getId())
                        .build();
            }
            return null;
        });

        // Xử lý lưu
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            try {
                if (user == null) {
                    if (userDAO.isUsernameExists(u.getUsername())) {
                        showAlert("Lỗi", "Username '" + u.getUsername() + "' đã tồn tại!");
                    } else {
                        userDAO.addUser(u);
                        loadData();
                    }
                } else {
                    userDAO.updateUser(u);
                    loadData();
                }
            } catch (Exception e) {
                showAlert("Lỗi Database", e.getMessage());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}