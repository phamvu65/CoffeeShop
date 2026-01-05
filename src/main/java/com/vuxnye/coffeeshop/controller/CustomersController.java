package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CustomerDAO;
import com.vuxnye.coffeeshop.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.util.Optional;

public class CustomersController {

    @FXML private TextField txtSearch;
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, Integer> colPoints;
    @FXML private TableColumn<Customer, Void> colAction;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredData;

    // --- ICONS ---
    private final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private final String SVG_DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";

    @FXML
    public void initialize() {
        setupTable();
        refreshData(); // Gọi hàm refreshData thay vì loadData
        setupSearch();
    }

    // [QUAN TRỌNG] Hàm này được thêm lại để sửa lỗi của bạn
    public void refreshData() {
        masterData.setAll(customerDAO.getAllCustomers());
    }

    private void setupTable() {
        // 1. CỘT TÊN
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-alignment: CENTER-LEFT;");
            }
        });

        // 2. CỘT SĐT
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-alignment: CENTER-LEFT;");
            }
        });

        // 3. CỘT ĐIỂM
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colPoints.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(String.format("%,d điểm", item));
                    lbl.setStyle("-fx-text-fill: #d97706; -fx-font-weight: 900; -fx-padding: 2 10; -fx-background-color: #fef3c7; -fx-background-radius: 10; -fx-font-size: 11px;");
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // 4. CỘT THAO TÁC
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            {
                String btnStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 50%; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px;";
                btnEdit.setStyle(btnStyle);
                btnDelete.setStyle(btnStyle);

                setupHover(btnEdit, "#eff6ff");
                setupHover(btnDelete, "#fef2f2");

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
                    btnEdit.setGraphic(createSVG(SVG_EDIT, "#3b82f6"));
                    btnEdit.setTooltip(new Tooltip("Sửa"));
                    btnDelete.setGraphic(createSVG(SVG_DELETE, "#ef4444"));
                    btnDelete.setTooltip(new Tooltip("Xóa"));
                    setGraphic(pane);
                }
            }
        });
    }

    private SVGPath createSVG(String content, String color) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.setStyle("-fx-fill: " + color + ";");
        svg.setScaleX(0.7); svg.setScaleY(0.7);
        return svg;
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, val) -> {
            filteredData.setPredicate(c -> {
                if(val == null || val.isEmpty()) return true;
                String lower = val.toLowerCase();
                return c.getName().toLowerCase().contains(lower) || c.getPhone().contains(lower);
            });
        });
        tblCustomers.setItems(filteredData);
    }

    @FXML private void handleAddNew() { showDialog(null); }
    private void handleEdit(Customer c) { showDialog(c); }

    private void handleDelete(Customer c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa khách hàng: " + c.getName() + "?");
        alert.setHeaderText(null);
        if(alert.showAndWait().get() == ButtonType.OK) {
            customerDAO.deleteCustomer(c.getId());
            refreshData(); // Gọi lại refreshData sau khi xóa
        }
    }

    private void showDialog(Customer c) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(c == null ? "Thêm khách hàng" : "Cập nhật thông tin");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        TextField txtName = new TextField(c != null ? c.getName() : "");
        TextField txtPhone = new TextField(c != null ? c.getPhone() : "");
        TextField txtPoints = new TextField(c != null ? String.valueOf(c.getPoints()) : "0");

        grid.add(new Label("Tên khách hàng (*):"), 0, 0); grid.add(txtName, 1, 0);
        grid.add(new Label("Số điện thoại (*):"), 0, 1);  grid.add(txtPhone, 1, 1);
        grid.add(new Label("Điểm tích lũy:"), 0, 2);      grid.add(txtPoints, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if(txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty()) return null;
                int points = 0;
                try { points = Integer.parseInt(txtPoints.getText().trim()); } catch (Exception e) {}

                Customer customer = new Customer();
                if (c != null) customer.setId(c.getId());
                customer.setName(txtName.getText().trim());
                customer.setPhone(txtPhone.getText().trim());
                customer.setPoints(points);
                return customer;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(customer -> {
            if (c == null) {
                if(customerDAO.isPhoneExists(customer.getPhone())) {
                    new Alert(Alert.AlertType.WARNING, "SĐT đã tồn tại!").show();
                } else {
                    customerDAO.addCustomer(customer);
                    refreshData(); // Gọi lại refreshData sau khi thêm
                }
            } else {
                customerDAO.updateCustomer(customer);
                refreshData(); // Gọi lại refreshData sau khi sửa
            }
        });
    }
}