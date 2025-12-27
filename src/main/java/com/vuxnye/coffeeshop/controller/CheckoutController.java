package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CustomerDAO;
import com.vuxnye.coffeeshop.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CheckoutController {

    @FXML private TextField txtPhone;
    @FXML private HBox boxCustomerInfo;
    @FXML private Label lblCustName, lblCustPoints, lblTotal;

    private double subtotal;
    private double discount = 0;
    private double finalTotal;

    private Customer currentCustomer = null;
    private CustomerDAO customerDAO = new CustomerDAO();
    private POSController parentController;

    public void setOrderData(double subtotal, POSController parent) {
        this.subtotal = subtotal;
        this.parentController = parent;
        updateTotalDisplay();
    }

    private void updateTotalDisplay() {
        double tax = subtotal * 0.1; // Thuế 10%
        this.finalTotal = subtotal + tax - discount;

        if (this.finalTotal < 0) this.finalTotal = 0;

        lblTotal.setText(String.format("%,.0f đ", finalTotal));
    }

    @FXML
    private void findCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) return;

        currentCustomer = customerDAO.findByPhone(phone);

        if (currentCustomer != null) {
            lblCustName.setText(currentCustomer.getName());
            lblCustPoints.setText("Điểm: " + currentCustomer.getPoints());
            boxCustomerInfo.setVisible(true);
            boxCustomerInfo.setManaged(true);
        } else {
            boolean confirm = showConfirm("Khách hàng mới", "SĐT này chưa có. Tạo khách hàng mới?");
            if (confirm) {
                // Tạo khách mới với điểm 0
                Customer newC = new Customer(0, "Khách mới", phone, 0);
                customerDAO.createCustomer(newC);
                findCustomer(); // Tìm lại để hiển thị
            }
        }
    }

    @FXML
    private void usePoints() {
        if (currentCustomer == null) return;

        // Logic: 1 điểm = 1000đ
        double maxDiscount = currentCustomer.getPoints() * 1000;
        double tax = subtotal * 0.1;

        // Không giảm quá tổng tiền (bao gồm thuế)
        double actualDiscount = Math.min(maxDiscount, subtotal + tax);

        this.discount = actualDiscount;
        updateTotalDisplay();

        boxCustomerInfo.setDisable(true);
    }

    @FXML
    private void payCash() { processPayment("TIỀN MẶT"); }

    @FXML
    private void payBank() { processPayment("CHUYỂN KHOẢN"); }

    private void processPayment(String method) {
        if (parentController != null) {
            // Callback về POSController để lưu DB
            parentController.onCheckoutCompleted(method, currentCustomer, discount, finalTotal);
        }
        closeModal();
    }

    @FXML
    private void closeModal() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }

    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}