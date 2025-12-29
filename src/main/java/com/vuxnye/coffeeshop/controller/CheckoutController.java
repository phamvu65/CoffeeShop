package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CustomerDAO;
import com.vuxnye.coffeeshop.dao.PromotionDAO;
import com.vuxnye.coffeeshop.model.Customer;
import com.vuxnye.coffeeshop.model.Promotion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Date;

public class CheckoutController {

    @FXML private TextField txtCustomerPhone;
    @FXML private Label lblCustomerName;
    @FXML private ComboBox<String> cbPaymentMethod;
    @FXML private Label lblSubtotal, lblTotal;

    // [MỚI] Các field cho voucher
    @FXML private TextField txtVoucher;
    @FXML private Label lblVoucherMessage; // Hiện thông báo lỗi/thành công
    @FXML private Label lblDiscount;

    private double subtotalAmount;
    private double discountAmount = 0; // Số tiền được giảm
    private double finalTotalAmount;

    private POSController posController;
    private Customer currentCustomer = null;

    private CustomerDAO customerDAO = new CustomerDAO();
    private PromotionDAO promoDAO = new PromotionDAO(); // DAO mới

    @FXML
    public void initialize() {
        cbPaymentMethod.getItems().addAll("TIỀN MẶT", "CHUYỂN KHOẢN");
        cbPaymentMethod.getSelectionModel().selectFirst();

        txtCustomerPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 10) {
                Customer c = customerDAO.getCustomerByPhone(newVal);
                if (c != null) {
                    currentCustomer = c;
                    lblCustomerName.setText(c.getName() + " (Điểm: " + c.getPoints() + ")");
                } else {
                    currentCustomer = null;
                    lblCustomerName.setText("Khách mới (Sẽ tạo tự động)");
                }
            } else {
                currentCustomer = null;
                lblCustomerName.setText("Khách lẻ");
            }
        });
    }

    public void setOrderData(double subtotal, POSController posController) {
        this.subtotalAmount = subtotal;
        this.posController = posController;
        updateTotals();
    }

    // [MỚI] Hàm xử lý áp mã Voucher
    @FXML
    private void applyVoucher() {
        String code = txtVoucher.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            resetDiscount("Vui lòng nhập mã code!", true);
            return;
        }

        Promotion promo = promoDAO.getPromotionByCode(code);

        // 1. Kiểm tra tồn tại
        if (promo == null) {
            resetDiscount("Mã giảm giá không tồn tại!", true);
            return;
        }

        // 2. Kiểm tra trạng thái Active
        if (!promo.isActive()) {
            resetDiscount("Chương trình này đang tạm dừng!", true);
            return;
        }

        // 3. Kiểm tra ngày hết hạn
        long now = System.currentTimeMillis();
        if (promo.getStartDate().getTime() > now || promo.getEndDate().getTime() < now) {
            resetDiscount("Mã này đã hết hạn hoặc chưa bắt đầu!", true);
            return;
        }

        // 4. Kiểm tra giá trị đơn tối thiểu
        if (subtotalAmount < promo.getMinOrderValue()) {
            resetDiscount("Đơn hàng phải từ " + String.format("%,.0f đ", promo.getMinOrderValue()) + " mới được dùng mã này!", true);
            return;
        }

        // 5. Tính toán tiền giảm
        double calculatedDiscount = 0;
        if ("PERCENT".equals(promo.getType())) {
            calculatedDiscount = subtotalAmount * (promo.getValue() / 100);
            // Kiểm tra giảm tối đa
            if (promo.getMaxDiscount() > 0 && calculatedDiscount > promo.getMaxDiscount()) {
                calculatedDiscount = promo.getMaxDiscount();
            }
        } else {
            // Giảm tiền mặt cố định
            calculatedDiscount = promo.getValue();
        }

        // Không được giảm quá giá trị đơn hàng
        if (calculatedDiscount > subtotalAmount) calculatedDiscount = subtotalAmount;

        // Áp dụng thành công
        this.discountAmount = calculatedDiscount;
        updateTotals();

        lblVoucherMessage.setText("Đã áp dụng mã: -" + String.format("%,.0f đ", discountAmount));
        lblVoucherMessage.setStyle("-fx-text-fill: #166534;"); // Xanh lá
        lblVoucherMessage.setVisible(true);
    }

    private void resetDiscount(String msg, boolean isError) {
        this.discountAmount = 0;
        updateTotals();
        lblVoucherMessage.setText(msg);
        lblVoucherMessage.setStyle("-fx-text-fill: " + (isError ? "#ef4444;" : "#166534;"));
        lblVoucherMessage.setVisible(true);
    }

    private void updateTotals() {
        finalTotalAmount = subtotalAmount - discountAmount;
        lblSubtotal.setText(String.format("%,.0f đ", subtotalAmount));
        lblDiscount.setText("- " + String.format("%,.0f đ", discountAmount)); // Hiện tiền giảm
        lblTotal.setText(String.format("%,.0f đ", finalTotalAmount));
    }

    @FXML
    private void confirmCheckout() {
        String method = cbPaymentMethod.getValue();

        if (currentCustomer == null && !txtCustomerPhone.getText().isEmpty()) {
            currentCustomer = new Customer(0, "Khách mới", txtCustomerPhone.getText(), 0);
            customerDAO.addCustomer(currentCustomer);
            currentCustomer = customerDAO.getCustomerByPhone(txtCustomerPhone.getText());
        }

        // Gửi thông tin về POSController (bao gồm cả discount)
        posController.onCheckoutCompleted(method, currentCustomer, discountAmount, finalTotalAmount);
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }
}