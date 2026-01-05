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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

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
    @FXML private TableColumn<User, String> colRole; // Sẽ style giống colCode/colStatus
    @FXML private TableColumn<User, String> colHourlyRate; // Sẽ style giống colValue
    @FXML private TableColumn<User, Void> colAction;

    // --- Data & DAO ---
    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    // --- SVG ICONS (Lấy từ mẫu của bạn) ---
    private final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private final String SVG_DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    // private final String SVG_PLAY = "M8 5v14l11-7z"; // Tạm chưa dùng cho nhân viên
    // private final String SVG_PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";

    @FXML
    public void initialize() {
        setupTable();
        refreshData();

        // Setup Search
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

    private void setupTable() {
        // 1. CỘT TÊN (Đậm, màu tối - Giống colName mẫu)
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullname()));
        colName.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
            }
        });

        // 2. CÁC CỘT THÔNG THƯỜNG
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        colGender.setCellValueFactory(data -> {
            Boolean gender = data.getValue().getGender();
            return new SimpleStringProperty((gender != null && gender) ? "Nam" : "Nữ");
        });
        colGender.setStyle("-fx-alignment: CENTER;");

        // 3. CỘT VAI TRÒ (Badge màu - Giống colCode/colStatus mẫu)
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoleName()));
        colRole.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    User u = getTableView().getItems().get(getIndex());
                    Label lbl = new Label(item);

                    // Logic màu sắc: Admin màu Tím, Staff màu Xanh
                    if (u.getRoleId() == RoleEnum.ADMIN.getId()) {
                        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #7E22CE; -fx-font-size: 11px; -fx-background-color: #F3E8FF; -fx-padding: 2 8; -fx-background-radius: 4;");
                    } else {
                        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1D4ED8; -fx-font-size: 11px; -fx-background-color: #DBEAFE; -fx-padding: 2 8; -fx-background-radius: 4;");
                    }

                    setGraphic(lbl);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 4. CỘT LƯƠNG/GIỜ (Đậm, căn phải - Giống colValue mẫu)
        colHourlyRate.setCellValueFactory(data -> {
            BigDecimal rate = data.getValue().getHourlyRate();
            if (rate == null) rate = BigDecimal.ZERO;
            String formatted = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(rate);
            return new SimpleStringProperty(formatted + "/h");
        });
        colHourlyRate.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-font-weight: 900; -fx-text-fill: #1e293b; -fx-alignment: CENTER-RIGHT; -fx-font-size: 13px;");
            }
        });

        // 5. CỘT THAO TÁC (Nút tròn, hiệu ứng hover - Giống colAction mẫu)
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                // Style cơ bản cho nút tròn 30px
                String btnStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 50%; " +
                        "-fx-cursor: hand; -fx-padding: 0; " +
                        "-fx-min-width: 32px; -fx-min-height: 32px; " +
                        "-fx-max-width: 32px; -fx-max-height: 32px;";

                btnEdit.setStyle(btnStyle);
                btnDelete.setStyle(btnStyle);

                // Setup hiệu ứng Hover
                setupHover(btnEdit, "#eff6ff"); // Hover xanh nhạt
                setupHover(btnDelete, "#fef2f2"); // Hover đỏ nhạt

                // Sự kiện
                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                pane.setAlignment(Pos.CENTER);
            }

            private void setupHover(Button btn, String color) {
                String base = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 50%; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px;";
                String hover = "-fx-background-color: " + color + "; -fx-border-color: #cbd5e1; -fx-border-radius: 50%; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px;";
                btn.setOnMouseEntered(e -> btn.setStyle(hover));
                btn.setOnMouseExited(e -> btn.setStyle(base));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Set Icon và Tooltip
                    btnEdit.setGraphic(createSVG(SVG_EDIT, "#3b82f6")); // Icon Xanh
                    btnEdit.setTooltip(new Tooltip("Chỉnh sửa"));

                    btnDelete.setGraphic(createSVG(SVG_DELETE, "#ef4444")); // Icon Đỏ
                    btnDelete.setTooltip(new Tooltip("Xóa nhân viên"));

                    setGraphic(pane);
                }
            }
        });
    }

    // --- Helper tạo SVG ---
    private SVGPath createSVG(String content, String color) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.setStyle("-fx-fill: " + color + ";");
        svg.setScaleX(0.7);
        svg.setScaleY(0.7);
        return svg;
    }

    public void refreshData() {
        masterData.setAll(userDAO.getAllActiveUsers());
    }

    // --- BUTTON HANDLERS ---
    @FXML private void handleAddNew() { showStaffDialog(null); }

    private void handleEdit(User user) { showStaffDialog(user); }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa nhân viên " + user.getFullname() + "?");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userDAO.deleteUser(user.getId());
            refreshData();
        }
    }

    // --- MODAL DIALOG (Giữ nguyên logic cũ nhưng code gọn lại) ---
    private void showStaffDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Thêm nhân viên" : "Cập nhật nhân viên");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        // Fields
        TextField txtFullname = new TextField(user != null ? user.getFullname() : "");
        TextField txtPhone = new TextField(user != null ? user.getPhone() : "");
        ComboBox<String> cbGender = new ComboBox<>();
        cbGender.getItems().addAll("Nam", "Nữ");
        cbGender.setValue(user != null && Boolean.TRUE.equals(user.getGender()) ? "Nam" : "Nữ");
        TextField txtRate = new TextField(user != null && user.getHourlyRate() != null ? String.valueOf(user.getHourlyRate().longValue()) : "25000");
        TextField txtUsername = new TextField(user != null ? user.getUsername() : "");
        if (user != null) txtUsername.setDisable(true);
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Để trống nếu không đổi...");
        ComboBox<RoleEnum> cbRole = new ComboBox<>();
        cbRole.getItems().addAll(RoleEnum.values());
        cbRole.setValue(user != null ? RoleEnum.fromId(user.getRoleId()) : RoleEnum.STAFF);

        // Layout
        grid.add(new Label("Họ tên (*):"), 0, 0);   grid.add(txtFullname, 1, 0);
        grid.add(new Label("SĐT:"), 0, 1);          grid.add(txtPhone, 1, 1);
        grid.add(new Label("Giới tính:"), 0, 2);    grid.add(cbGender, 1, 2);
        grid.add(new Label("Lương/Giờ:"), 0, 3);    grid.add(txtRate, 1, 3);
        grid.add(new Label("Username (*):"), 0, 4); grid.add(txtUsername, 1, 4);
        grid.add(new Label("Mật khẩu:"), 0, 5);     grid.add(txtPassword, 1, 5);
        grid.add(new Label("Chức vụ:"), 0, 6);      grid.add(cbRole, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Result Converter
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                if (txtFullname.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty()) return null;
                BigDecimal rateVal;
                try {
                    rateVal = new BigDecimal(txtRate.getText().trim().replaceAll("[^\\d]", ""));
                } catch (Exception e) { rateVal = BigDecimal.ZERO; }

                return User.builder()
                        .id(user != null ? user.getId() : 0)
                        .fullname(txtFullname.getText().trim())
                        .phone(txtPhone.getText().trim())
                        .gender(cbGender.getValue().equals("Nam"))
                        .hourlyRate(rateVal)
                        .username(txtUsername.getText().trim())
                        .password(txtPassword.getText())
                        .roleId(cbRole.getValue().getId())
                        .build();
            }
            return null;
        });

        // Handle Save
        dialog.showAndWait().ifPresent(u -> {
            try {
                if (user == null) {
                    if (userDAO.isUsernameExists(u.getUsername())) showAlert("Lỗi", "Username đã tồn tại!");
                    else { userDAO.addUser(u); refreshData(); }
                } else {
                    userDAO.updateUser(u); refreshData();
                }
            } catch (Exception e) { showAlert("Lỗi", e.getMessage()); }
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