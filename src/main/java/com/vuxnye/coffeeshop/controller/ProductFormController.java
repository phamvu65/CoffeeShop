package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CategoryDAO;
import com.vuxnye.coffeeshop.dao.ProductDAO;
import com.vuxnye.coffeeshop.model.Category;
import com.vuxnye.coffeeshop.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;

public class ProductFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName, txtPrice, txtImage;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private ImageView imgPreview;
    @FXML private Label lblImgPlaceholder;

    private Product currentProduct;
    private MenuController parentController;

    private CategoryDAO categoryDAO = new CategoryDAO();
    private ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        // Load danh mục
        List<Category> categories = categoryDAO.getAllCategories();
        cbCategory.getItems().addAll(categories);

        // Preview ảnh ngay khi gõ
        txtImage.textProperty().addListener((obs, oldVal, newVal) -> {
            loadPreviewImage(newVal);
        });
    }

    // --- LOGIC XEM TRƯỚC ẢNH ---
    private void loadPreviewImage(String path) {
        if (path == null || path.trim().isEmpty()) {
            imgPreview.setImage(null);
            lblImgPlaceholder.setVisible(true);
            return;
        }

        try {
            Image image = null;
            // Case 1: Online
            if (path.startsWith("http")) {
                image = new Image(path, true);
            }
            // Case 2: Local
            else {
                InputStream is = getClass().getResourceAsStream("/images/" + path);
                if (is != null) {
                    image = new Image(is);
                }
            }

            if (image != null && !image.isError()) {
                imgPreview.setImage(image);
                lblImgPlaceholder.setVisible(false);
            } else {
                imgPreview.setImage(null);
                lblImgPlaceholder.setVisible(true);
            }
        } catch (Exception e) {
            imgPreview.setImage(null);
            lblImgPlaceholder.setVisible(true);
        }
    }

    public void setProductData(Product p) {
        this.currentProduct = p;
        if (p != null) {
            // Chế độ Sửa
            lblTitle.setText("Chỉnh sửa món ăn");
            txtName.setText(p.getName());
            txtPrice.setText(String.valueOf((int)p.getPrice()));
            txtImage.setText(p.getImagePath());

            // Kích hoạt preview ảnh cũ
            loadPreviewImage(p.getImagePath());

            // Chọn đúng danh mục cũ
            for(Category c : cbCategory.getItems()) {
                if(c.getId() == p.getCategoryId()) {
                    cbCategory.setValue(c);
                    break;
                }
            }
        } else {
            // Chế độ Thêm mới
            lblTitle.setText("Thêm món ăn mới");
            cbCategory.getSelectionModel().selectFirst();
            lblImgPlaceholder.setVisible(true);
        }
    }

    public void setParentController(MenuController parent) {
        this.parentController = parent;
    }

    @FXML
    private void saveProduct() {
        // Validate đơn giản
        if (txtName.getText().isEmpty() || txtPrice.getText().isEmpty() || cbCategory.getValue() == null) {
            showAlert("Thiếu thông tin", "Vui lòng nhập tên, giá và chọn danh mục!");
            return;
        }

        try {
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            int catId = cbCategory.getValue().getId();
            String imgPath = txtImage.getText().trim();

            if (currentProduct == null) {
                // ADD
                Product newP = new Product(0, name, catId, price, "Ly", imgPath, "", true, "");
                productDAO.addProduct(newP);
            } else {
                // UPDATE
                currentProduct.setName(name);
                currentProduct.setPrice(price);
                currentProduct.setCategoryId(catId);
                currentProduct.setImagePath(imgPath);
                productDAO.updateProduct(currentProduct);
            }

            // Refresh bảng ở form cha
            if (parentController != null) parentController.refreshData();
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Giá bán phải là số!");
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