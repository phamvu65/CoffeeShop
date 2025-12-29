package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.PromotionDAO;
import com.vuxnye.coffeeshop.model.Promotion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class PromotionFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName, txtCode, txtValue, txtMinOrder, txtMaxDiscount;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker dpStart, dpEnd;

    private Promotion currentPromo;
    private PromotionsController parentController;
    private PromotionDAO promoDAO = new PromotionDAO();

    @FXML
    public void initialize() {
        cbType.getItems().addAll("Phần trăm (%)", "Số tiền cố định");
        cbType.getSelectionModel().selectFirst();

        // Mặc định ngày
        dpStart.setValue(LocalDate.now());
        dpEnd.setValue(LocalDate.now().plusDays(30));
    }

    public void setPromoData(Promotion p) {
        this.currentPromo = p;
        if (p != null) {
            lblTitle.setText("Cập nhật khuyến mãi");
            txtName.setText(p.getDescription());
            txtCode.setText(p.getCode());
            txtValue.setText(String.valueOf((int)p.getValue()));
            txtMinOrder.setText(String.valueOf((int)p.getMinOrderValue()));
            txtMaxDiscount.setText(String.valueOf((int)p.getMaxDiscount()));

            cbType.getSelectionModel().select("PERCENT".equals(p.getType()) ? 0 : 1);

            if(p.getStartDate() != null) dpStart.setValue(p.getStartDate().toLocalDate());
            if(p.getEndDate() != null) dpEnd.setValue(p.getEndDate().toLocalDate());
        } else {
            lblTitle.setText("Tạo khuyến mãi mới");
        }
    }

    public void setParentController(PromotionsController parent) {
        this.parentController = parent;
    }

    @FXML
    private void savePromotion() {
        if (txtName.getText().isEmpty() || txtCode.getText().isEmpty() || txtValue.getText().isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng nhập tên, mã và giá trị giảm!");
            return;
        }

        try {
            String name = txtName.getText();
            String code = txtCode.getText().toUpperCase();
            String type = cbType.getSelectionModel().getSelectedIndex() == 0 ? "PERCENT" : "FIXED";
            double value = Double.parseDouble(txtValue.getText());
            double minOrder = txtMinOrder.getText().isEmpty() ? 0 : Double.parseDouble(txtMinOrder.getText());
            double maxDiscount = txtMaxDiscount.getText().isEmpty() ? 0 : Double.parseDouble(txtMaxDiscount.getText());

            Date start = Date.valueOf(dpStart.getValue());
            Date end = Date.valueOf(dpEnd.getValue());

            if (currentPromo == null) {
                Promotion newP = new Promotion(0, code, name, type, value, minOrder, maxDiscount, start, end, true);
                promoDAO.addPromotion(newP);
            } else {
                currentPromo.setDescription(name);
                currentPromo.setCode(code);
                currentPromo.setType(type);
                currentPromo.setValue(value);
                currentPromo.setMinOrderValue(minOrder);
                currentPromo.setMaxDiscount(maxDiscount);
                currentPromo.setStartDate(start);
                currentPromo.setEndDate(end);
                promoDAO.updatePromotion(currentPromo);
            }

            if (parentController != null) parentController.refreshData();
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Các trường số phải nhập đúng định dạng!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}