package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CustomerDAO;
import com.vuxnye.coffeeshop.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CustomerFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtPoints;

    private CustomerDAO customerDAO = new CustomerDAO();
    private Customer currentCustomer; // Nếu null là Thêm mới, có dữ liệu là Sửa
    private CustomersController parentController;

    public void setParentController(CustomersController parentController) {
        this.parentController = parentController;
    }

    public void setCustomerData(Customer customer) {
        this.currentCustomer = customer;
        if (customer != null) {
            // Chế độ EDIT
            lblTitle.setText("Chỉnh sửa thông tin");
            txtName.setText(customer.getName());
            txtPhone.setText(customer.getPhone());
            txtPoints.setText(String.valueOf(customer.getPoints()));
        } else {
            // Chế độ ADD
            lblTitle.setText("Thêm khách hàng mới");
            txtName.clear();
            txtPhone.clear();
            txtPoints.setText("0");
        }
    }

    @FXML
    private void handleSave() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String pointsStr = txtPoints.getText().trim();

        // Validation
        if (name.isEmpty() || phone.isEmpty()) {
            showAlert("Vui lòng nhập đầy đủ tên và số điện thoại!");
            return;
        }

        int points = 0;
        try {
            points = Integer.parseInt(pointsStr);
            if (points < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Điểm tích lũy phải là số nguyên dương!");
            return;
        }

        if (currentCustomer == null) {
            // Thêm mới
            // Kiểm tra trùng SĐT
            if (customerDAO.getCustomerByPhone(phone) != null) {
                showAlert("Số điện thoại này đã tồn tại trong hệ thống!");
                return;
            }

            Customer newC = new Customer(0, name, phone, points);
            customerDAO.addCustomer(newC);
        } else {
            // Cập nhật
            currentCustomer.setName(name);
            currentCustomer.setPhone(phone);
            currentCustomer.setPoints(points);
            customerDAO.updateCustomer(currentCustomer);
        }

        // Refresh bảng ở màn hình cha
        if (parentController != null) {
            parentController.refreshData();
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}