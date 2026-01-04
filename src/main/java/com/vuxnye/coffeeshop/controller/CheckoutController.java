package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CustomerDAO;
import com.vuxnye.coffeeshop.dao.PromotionDAO;
import com.vuxnye.coffeeshop.model.Customer;
import com.vuxnye.coffeeshop.model.Promotion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CheckoutController {

    // --- Khách hàng ---
    @FXML private TextField txtCustomerPhone;
    @FXML private VBox boxCustomerName;
    @FXML private TextField txtCustomerName;
    @FXML private Label lblCustomerStatus;

    // --- Voucher ---
    @FXML private TextField txtVoucher;
    @FXML private Label lblVoucherMessage;

    // --- Thanh toán ---
    @FXML private ComboBox<String> cbPaymentMethod;
    @FXML private Label lblSubtotal, lblTotal, lblDiscount;

    private double subtotalAmount;
    private double discountAmount = 0;
    private double finalTotalAmount;

    private POSController posController;
    private Customer currentCustomer = null;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PromotionDAO promoDAO = new PromotionDAO();

    // Quy đổi điểm: 10.000 VNĐ = 1 điểm
    private final double POINTS_CONVERSION_RATE = 10000.0;

    @FXML
    public void initialize() {
        // Setup ComboBox
        cbPaymentMethod.getItems().addAll("TIỀN MẶT", "CHUYỂN KHOẢN", "THẺ NGÂN HÀNG");
        cbPaymentMethod.getSelectionModel().selectFirst();

        // Listener: Tìm kiếm khách hàng khi nhập SĐT
        txtCustomerPhone.textProperty().addListener((obs, oldVal, newVal) -> handleCustomerSearch(newVal));
    }

    private void handleCustomerSearch(String phone) {
        // Ẩn/Hiện box nhập tên
        boxCustomerName.setVisible(false);
        boxCustomerName.setManaged(false);
        lblCustomerStatus.setText("");

        if (phone.length() >= 10) {
            Customer c = customerDAO.getCustomerByPhone(phone);

            if (c != null) {
                // KHÁCH CŨ: Tìm thấy
                currentCustomer = c;
                txtCustomerName.setText(c.getName());

                lblCustomerStatus.setText("✓ Khách thành viên - Điểm tích lũy: " + String.format("%,d", c.getPoints()));
                lblCustomerStatus.setStyle("-fx-text-fill: #166534;"); // Xanh lá

                // Ẩn ô nhập tên vì đã có tên
                boxCustomerName.setVisible(false);
                boxCustomerName.setManaged(false);
            } else {
                // KHÁCH MỚI: Chưa tìm thấy
                currentCustomer = null;
                txtCustomerName.clear();

                lblCustomerStatus.setText("! Khách hàng mới - Vui lòng nhập tên để tạo tài khoản");
                lblCustomerStatus.setStyle("-fx-text-fill: #d97706;"); // Cam

                // Hiện ô nhập tên
                boxCustomerName.setVisible(true);
                boxCustomerName.setManaged(true);
            }
        } else {
            currentCustomer = null;
        }
    }

    public void setOrderData(double subtotal, POSController posController) {
        this.subtotalAmount = subtotal;
        this.posController = posController;
        updateTotals();
    }

    @FXML
    private void applyVoucher() {
        String code = txtVoucher.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showVoucherMsg("Vui lòng nhập mã!", true);
            return;
        }

        Promotion promo = promoDAO.getPromotionByCode(code);

        if (promo == null) {
            showVoucherMsg("Mã không tồn tại!", true);
            discountAmount = 0;
        } else if (!promo.isActive()) {
            showVoucherMsg("Chương trình đã kết thúc!", true);
            discountAmount = 0;
        } else if (subtotalAmount < promo.getMinOrderValue()) {
            showVoucherMsg("Đơn hàng chưa đủ điều kiện tối thiểu!", true);
            discountAmount = 0;
        } else {
            // Tính toán giảm giá
            if ("PERCENT".equals(promo.getType())) {
                discountAmount = subtotalAmount * (promo.getValue() / 100);
                if (promo.getMaxDiscount() > 0 && discountAmount > promo.getMaxDiscount()) {
                    discountAmount = promo.getMaxDiscount();
                }
            } else {
                discountAmount = promo.getValue();
            }

            if(discountAmount > subtotalAmount) discountAmount = subtotalAmount;

            showVoucherMsg("Áp dụng mã thành công: -" + String.format("%,.0f đ", discountAmount), false);
        }
        updateTotals();
    }

    private void showVoucherMsg(String msg, boolean isError) {
        lblVoucherMessage.setText(msg);
        lblVoucherMessage.setStyle("-fx-text-fill: " + (isError ? "#ef4444;" : "#166534;"));
        lblVoucherMessage.setVisible(true);
    }

    private void updateTotals() {
        finalTotalAmount = subtotalAmount - discountAmount;
        lblSubtotal.setText(String.format("%,.0f đ", subtotalAmount));
        lblDiscount.setText("- " + String.format("%,.0f đ", discountAmount));
        lblTotal.setText(String.format("%,.0f đ", finalTotalAmount));
    }

    @FXML
    private void confirmCheckout() {
        String phone = txtCustomerPhone.getText().trim();
        String name = txtCustomerName.getText().trim();
        String method = cbPaymentMethod.getValue();

        // 1. Xử lý logic Khách hàng
        if (!phone.isEmpty()) {
            if (currentCustomer == null) {
                // Nếu là khách mới -> Bắt buộc nhập tên
                if (name.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập Tên khách hàng mới!");
                    alert.showAndWait();
                    return;
                }
                // Tạo khách mới vào DB
                Customer newCust = new Customer(0, name, phone, 0);
                customerDAO.addCustomer(newCust);
                // Lấy lại ID vừa tạo từ DB
                currentCustomer = customerDAO.getCustomerByPhone(phone);
            }

            // 2. Tính điểm thưởng (10k = 1 điểm)
            int pointsEarned = (int) (finalTotalAmount / POINTS_CONVERSION_RATE);
            if (pointsEarned > 0 && currentCustomer != null) {
                customerDAO.addPoints(currentCustomer.getId(), pointsEarned);
                System.out.println("Đã cộng " + pointsEarned + " điểm cho khách " + currentCustomer.getName());
            }
        }

        // 3. Gửi dữ liệu về POS và đóng
        posController.onCheckoutCompleted(method, currentCustomer, discountAmount, finalTotalAmount);
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }
}