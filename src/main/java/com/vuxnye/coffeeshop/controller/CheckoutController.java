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

    // --- Voucher & Điểm ---
    @FXML private TextField txtVoucher;
    @FXML private Label lblVoucherMessage;

    // [MỚI] Checkbox để tích chọn dùng điểm
    @FXML private CheckBox chkUsePoints;

    // --- Thanh toán ---
    @FXML private ComboBox<String> cbPaymentMethod;
    @FXML private Label lblSubtotal, lblTotal, lblDiscount;

    // --- Biến tính toán ---
    private double subtotalAmount;
    private double voucherDiscount = 0;
    private double pointDiscount = 0;
    private double finalTotalAmount;

    // [MỚI] Lưu số điểm thực tế sẽ bị trừ
    private int pointsUsed = 0;

    private POSController posController;
    private Customer currentCustomer = null;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PromotionDAO promoDAO = new PromotionDAO();

    // Tỷ lệ tích điểm: 10.000 VNĐ mua hàng = 1 điểm
    private final double POINTS_EARN_RATE = 10000.0;

    // [MỚI] Tỷ lệ tiêu điểm: 1 điểm = 100 VNĐ (tức là 100 điểm = 10.000 VNĐ)
    private final double POINT_VALUE = 100.0;

    @FXML
    public void initialize() {
        cbPaymentMethod.getItems().addAll("TIỀN MẶT", "CHUYỂN KHOẢN", "THẺ NGÂN HÀNG");
        cbPaymentMethod.getSelectionModel().selectFirst();

        txtCustomerPhone.textProperty().addListener((obs, oldVal, newVal) -> handleCustomerSearch(newVal));

        // [MỚI] Lắng nghe sự kiện tick vào ô dùng điểm
        if (chkUsePoints != null) {
            chkUsePoints.setDisable(true); // Mặc định khóa lại
            chkUsePoints.selectedProperty().addListener((obs, oldVal, isSelected) -> calculateTotals());
        }
    }

    private void handleCustomerSearch(String phone) {
        boxCustomerName.setVisible(false);
        boxCustomerName.setManaged(false);
        lblCustomerStatus.setText("");

        // Reset điểm khi tìm khách mới
        if (chkUsePoints != null) {
            chkUsePoints.setSelected(false);
            chkUsePoints.setDisable(true);
            chkUsePoints.setText("Dùng điểm tích lũy");
        }

        if (phone.length() >= 10) {
            Customer c = customerDAO.getCustomerByPhone(phone);

            if (c != null) {
                // KHÁCH CŨ
                currentCustomer = c;
                txtCustomerName.setText(c.getName());

                lblCustomerStatus.setText("✓ Thành viên: " +  currentCustomer.getName() + " - Điểm tích lũy: " + c.getPoints());
                lblCustomerStatus.setStyle("-fx-text-fill: #166534;");

                // [MỚI] Xử lý Checkbox điểm
                if (chkUsePoints != null) {
                    double maxDiscount = c.getPoints() * POINT_VALUE; // Quy đổi ra tiền
                    if (maxDiscount > 0) {
                        chkUsePoints.setDisable(false);
                        chkUsePoints.setText(String.format("Dùng %s điểm (-%,.0f đ)",
                                String.format("%,d", c.getPoints()), maxDiscount));
                    } else {
                        chkUsePoints.setText("Không đủ điểm tích lũy");
                    }
                }

                boxCustomerName.setVisible(false);
                boxCustomerName.setManaged(false);
            } else {
                // KHÁCH MỚI
                currentCustomer = null;
                txtCustomerName.clear();
                lblCustomerStatus.setText("! Khách hàng mới - Vui lòng nhập tên");
                lblCustomerStatus.setStyle("-fx-text-fill: #d97706;");
                boxCustomerName.setVisible(true);
                boxCustomerName.setManaged(true);
            }
        } else {
            currentCustomer = null;
        }
        calculateTotals(); // Tính lại tiền (để reset về giá gốc nếu đổi khách)
    }

    public void setOrderData(double subtotal, POSController posController) {
        this.subtotalAmount = subtotal;
        this.posController = posController;
        calculateTotals();
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
            voucherDiscount = 0;
        } else if (!promo.isActive()) {
            showVoucherMsg("Chương trình đã kết thúc!", true);
            voucherDiscount = 0;
        } else if (subtotalAmount < promo.getMinOrderValue()) {
            showVoucherMsg("Đơn chưa đủ điều kiện!", true);
            voucherDiscount = 0;
        } else {
            if ("PERCENT".equals(promo.getType())) {
                voucherDiscount = subtotalAmount * (promo.getValue() / 100);
                if (promo.getMaxDiscount() > 0 && voucherDiscount > promo.getMaxDiscount()) {
                    voucherDiscount = promo.getMaxDiscount();
                }
            } else {
                voucherDiscount = promo.getValue();
            }

            // Voucher không được giảm quá tổng tiền
            if(voucherDiscount > subtotalAmount) voucherDiscount = subtotalAmount;

            showVoucherMsg("Áp dụng mã: -" + String.format("%,.0f đ", voucherDiscount), false);
        }
        calculateTotals();
    }

    // [QUAN TRỌNG] Hàm tính toán tổng hợp (Voucher + Điểm)
    private void calculateTotals() {
        // 1. Trừ Voucher trước
        double tempTotal = subtotalAmount - voucherDiscount;
        if (tempTotal < 0) tempTotal = 0;

        // 2. Trừ Điểm tích lũy (Nếu tick chọn)
        pointDiscount = 0;
        pointsUsed = 0;

        if (chkUsePoints != null && chkUsePoints.isSelected() && currentCustomer != null) {
            double maxPointValue = currentCustomer.getPoints() * POINT_VALUE; // Tổng tiền từ điểm khách có

            // Nếu tiền điểm > tiền cần thanh toán -> Chỉ trừ đủ số tiền cần thanh toán
            if (maxPointValue >= tempTotal) {
                pointDiscount = tempTotal;
                // Tính ngược lại số điểm cần trừ (làm tròn lên)
                pointsUsed = (int) Math.ceil(pointDiscount / POINT_VALUE);
            } else {
                // Nếu tiền điểm ít hơn -> Trừ hết điểm
                pointDiscount = maxPointValue;
                pointsUsed = currentCustomer.getPoints();
            }
        }

        // 3. Tính tổng cuối
        finalTotalAmount = tempTotal - pointDiscount;
        if (finalTotalAmount < 0) finalTotalAmount = 0;

        // 4. Hiển thị
        lblSubtotal.setText(String.format("%,.0f đ", subtotalAmount));

        // Hiển thị tổng giảm giá (Voucher + Điểm)
        double totalDiscount = voucherDiscount + pointDiscount;
        lblDiscount.setText("- " + String.format("%,.0f đ", totalDiscount));

        lblTotal.setText(String.format("%,.0f đ", finalTotalAmount));
    }

    private void showVoucherMsg(String msg, boolean isError) {
        lblVoucherMessage.setText(msg);
        lblVoucherMessage.setStyle("-fx-text-fill: " + (isError ? "#ef4444;" : "#166534;"));
        lblVoucherMessage.setVisible(true);
    }

    @FXML
    private void confirmCheckout() {
        String phone = txtCustomerPhone.getText().trim();
        String name = txtCustomerName.getText().trim();
        String method = cbPaymentMethod.getValue();

        // Xử lý khách hàng
        if (!phone.isEmpty()) {
            if (currentCustomer == null) {
                if (name.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Vui lòng nhập Tên khách hàng mới!").show();
                    return;
                }
                Customer newCust = new Customer(0, name, phone, 0);
                customerDAO.addCustomer(newCust);
                currentCustomer = customerDAO.getCustomerByPhone(phone);
            }

            // [MỚI] 1. Trừ điểm tích lũy (Nếu có dùng)
            if (pointsUsed > 0 && currentCustomer != null) {
                // Trừ điểm: dùng số âm
                customerDAO.addPoints(currentCustomer.getId(), -pointsUsed);
                System.out.println("Đã trừ " + pointsUsed + " điểm của khách.");
            }

            // [MỚI] 2. Cộng điểm thưởng mới (Dựa trên số tiền thực trả)
            // (Chỉ được tích điểm trên số tiền thực trả sau khi đã trừ voucher và điểm cũ)
            int pointsEarned = (int) (finalTotalAmount / POINTS_EARN_RATE);
            if (pointsEarned > 0 && currentCustomer != null) {
                customerDAO.addPoints(currentCustomer.getId(), pointsEarned);
                System.out.println("Đã cộng " + pointsEarned + " điểm mới.");
            }
        }

        // Gửi về POS (Tổng giảm giá = voucher + điểm)
        posController.onCheckoutCompleted(method, currentCustomer, (voucherDiscount + pointDiscount), finalTotalAmount);
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }
}