package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CategoryDAO;
import com.vuxnye.coffeeshop.dao.ProductDAO;
import com.vuxnye.coffeeshop.model.Category;
import com.vuxnye.coffeeshop.model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class ProductFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName, txtPrice;
    @FXML private ComboBox<Category> cbCategory;

    // [QUAN TRỌNG] Dùng tên biến khớp với FXML mới nhất
    @FXML private TextField txtImagePath;
    @FXML private ImageView imgPreview;
    @FXML private Label lblImgPlaceholder;

    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private Product currentProduct;
    private MenuController parentController;

    @FXML
    public void initialize() {
        loadCategories();

        // [KHÔI PHỤC LOGIC CŨ] Lắng nghe khi nhập/paste URL để load ảnh ngay
        txtImagePath.textProperty().addListener((obs, oldVal, newVal) -> {
            loadPreviewImage(newVal);
        });
    }

    // --- LOGIC XỬ LÝ ẢNH (KẾT HỢP CŨ & MỚI) ---
    private void loadPreviewImage(String path) {
        if (path == null || path.trim().isEmpty()) {
            imgPreview.setImage(null);
            lblImgPlaceholder.setVisible(true);
            lblImgPlaceholder.setText("Chưa chọn ảnh");
            return;
        }

        // Chạy trong Thread để không đơ giao diện nếu mạng lag
        new Thread(() -> {
            try {
                Image image = null;
                String cleanPath = path.trim();

                // 1. Nếu là URL Online (Logic cũ của bạn + Fix User-Agent)
                if (cleanPath.startsWith("http://") || cleanPath.startsWith("https://")) {
                    // Dùng constructor này của JavaFX để load background, rất mượt
                    image = new Image(cleanPath, true);
                }
                // 2. Nếu là File Local (Logic mới)
                else {
                    InputStream is = getClass().getResourceAsStream("/images/" + cleanPath);
                    if (is != null) {
                        image = new Image(is);
                    }
                }

                // Cập nhật giao diện
                Image finalImage = image;
                Platform.runLater(() -> {
                    if (finalImage != null && !finalImage.isError()) {
                        imgPreview.setImage(finalImage);
                        lblImgPlaceholder.setVisible(false);
                    } else {
                        // Nếu đang load (background loading) thì chưa lỗi ngay, chờ tí
                        if(finalImage != null && finalImage.getProgress() < 1.0) {
                            lblImgPlaceholder.setText("Đang tải...");
                            // Bind image để khi load xong nó tự hiện
                            imgPreview.setImage(finalImage);
                            lblImgPlaceholder.setVisible(false);
                        } else {
                            imgPreview.setImage(null);
                            lblImgPlaceholder.setVisible(true);
                            lblImgPlaceholder.setText("Không tìm thấy ảnh");
                        }
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    imgPreview.setImage(null);
                    lblImgPlaceholder.setVisible(true);
                });
            }
        }).start();
    }

    // Nút chọn file từ máy tính (Tính năng mới)
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(txtName.getScene().getWindow());

        if (file != null) {
            try {
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists()) destDir.mkdirs();
                File destFile = new File(destDir, file.getName());
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Gán tên file vào TextField -> Listener ở initialize sẽ tự động gọi loadPreviewImage
                txtImagePath.setText(file.getName());

            } catch (IOException e) {
                showAlert("Lỗi", "Không thể lưu ảnh: " + e.getMessage());
            }
        }
    }

    // --- CÁC CHỨC NĂNG KHÁC (GIỮ NGUYÊN) ---

    public void setProductData(Product p) {
        this.currentProduct = p;
        if (p != null) {
            lblTitle.setText("Chỉnh sửa món");
            txtName.setText(p.getName());
            txtPrice.setText(String.valueOf((int)p.getPrice()));

            // Set text sẽ kích hoạt listener load ảnh
            if (p.getImagePath() != null) txtImagePath.setText(p.getImagePath());

            for(Category c : cbCategory.getItems()) {
                if(c.getName().equals(p.getCategoryName())) {
                    cbCategory.getSelectionModel().select(c); break;
                }
            }
        } else {
            lblTitle.setText("Thêm món mới");
            cbCategory.getSelectionModel().selectFirst();
            txtImagePath.clear();
        }
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || txtPrice.getText().isEmpty() || cbCategory.getValue() == null) {
            showAlert("Thiếu thông tin", "Vui lòng nhập tên, giá và chọn danh mục!");
            return;
        }

        try {
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            Category cat = cbCategory.getValue();
            String imgPath = txtImagePath.getText().trim();

            if (currentProduct == null) {
                // Constructor này cần khớp với Product.java của bạn
                Product newP = new Product(0, name, cat.getId(), cat.getName(), price, imgPath, true);
                productDAO.addProduct(newP);
            } else {
                currentProduct.setName(name);
                currentProduct.setPrice(price);
                currentProduct.setCategoryId(cat.getId());
                currentProduct.setCategoryName(cat.getName());
                currentProduct.setImagePath(imgPath);
                productDAO.updateProduct(currentProduct);
            }

            if (parentController != null) parentController.refreshData();
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Giá bán phải là số!");
        }
    }

    // --- QUẢN LÝ DANH MỤC ---
    @FXML private void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm danh mục");
        dialog.setHeaderText(null);
        dialog.setContentText("Nhập tên danh mục:");
        dialog.showAndWait().ifPresent(name -> {
            if(!name.trim().isEmpty() && !categoryDAO.isNameExists(name.trim())) {
                categoryDAO.addCategory(name.trim());
                loadCategories();
                cbCategory.getSelectionModel().selectLast();
            }
        });
    }

    @FXML private void handleEditCategory() {
        Category sel = cbCategory.getValue();
        if(sel == null) return;
        TextInputDialog dialog = new TextInputDialog(sel.getName());
        dialog.setTitle("Sửa danh mục");
        dialog.setHeaderText(null);
        dialog.setContentText("Nhập tên mới:");
        dialog.showAndWait().ifPresent(name -> {
            if(!name.trim().isEmpty()) {
                sel.setName(name.trim());
                categoryDAO.updateCategory(sel);
                loadCategories();
            }
        });
    }

    @FXML private void handleDeleteCategory() {
        Category sel = cbCategory.getValue();
        if(sel == null) return;
        if(categoryDAO.hasProducts(sel.getId())) {
            showAlert("Lỗi", "Không thể xóa danh mục đang có sản phẩm!");
            return;
        }
        Alert al = new Alert(Alert.AlertType.CONFIRMATION, "Xóa danh mục này?");
        if(al.showAndWait().get() == ButtonType.OK) {
            categoryDAO.deleteCategory(sel.getId());
            loadCategories();
        }
    }

    private void loadCategories() {
        Category sel = cbCategory.getValue();
        cbCategory.setItems(FXCollections.observableArrayList(categoryDAO.getAllCategories()));
        if(sel != null) {
            for(Category c : cbCategory.getItems()) if(c.getId() == sel.getId()) cbCategory.setValue(c);
        }
    }

    public void setParentController(MenuController parent) { this.parentController = parent; }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) txtName.getScene().getWindow()).close(); }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}