package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CategoryDAO;
import com.vuxnye.coffeeshop.dao.ProductDAO;
import com.vuxnye.coffeeshop.model.Category;
import com.vuxnye.coffeeshop.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MenuController {

    // --- FXML UI COMPONENTS ---
    @FXML private TextField txtSearch;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private TableView<Product> tblProducts;

    @FXML private TableColumn<Product, String> colName, colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Void> colImage, colStatus, colAction;

    // Controls Phân trang
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label lblPageInfo;

    // --- DATA & DAO ---
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    // Biến quản lý phân trang & Lọc
    private int currentPage = 1;
    private final int itemsPerPage = 8; // Số món mỗi trang
    private int totalPages = 1;
    private int currentCategoryId = 0; // 0 = Tất cả

    // --- SVG ICONS ---
    private final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private final String SVG_DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    private final String SVG_VISIBLE = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private final String SVG_HIDDEN = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27z";

    @FXML
    public void initialize() {
        setupTable();

        // 1. Tải danh mục vào ComboBox
        loadCategories();

        // 2. Tính toán & Load dữ liệu lần đầu
        calculatePagination();
        loadData();

        // 3. Sự kiện Tìm kiếm
        txtSearch.textProperty().addListener((obs, old, val) -> {
            currentPage = 1; // Reset về trang 1
            loadData();
        });

        // 4. Sự kiện Chọn danh mục
        cbCategory.setOnAction(e -> {
            Category selected = cbCategory.getValue();
            if (selected != null) {
                currentCategoryId = selected.getId();
                currentPage = 1; // Reset về trang 1
                calculatePagination(); // Tính lại tổng số trang cho danh mục mới
                loadData();
            }
        });
    }

    // --- LOGIC LOAD DỮ LIỆU ---

    private void loadCategories() {
        List<Category> list = categoryDAO.getAllCategories();
        // Thêm tùy chọn mặc định
        list.add(0, new Category(0, "Tất cả danh mục"));

        ObservableList<Category> categories = FXCollections.observableArrayList(list);
        cbCategory.setItems(categories);
        cbCategory.getSelectionModel().selectFirst();

        // Định dạng hiển thị tên trong ComboBox
        cbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category c) { return c == null ? null : c.getName(); }
            @Override
            public Category fromString(String string) { return null; }
        });
    }

    private void calculatePagination() {
        // Đếm tổng sản phẩm theo danh mục đang chọn
        int totalItems = productDAO.countTotalProducts(currentCategoryId);
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
    }

    private void loadData() {
        String keyword = txtSearch.getText().trim();
        List<Product> list;

        if (keyword.isEmpty()) {
            // Chế độ: Phân trang + Lọc danh mục
            int offset = (currentPage - 1) * itemsPerPage;
            list = productDAO.getProductsPaging(itemsPerPage, offset, currentCategoryId);
            updatePaginationControls(false);
        } else {
            // Chế độ: Tìm kiếm (Lấy hết kết quả) + Lọc danh mục
            list = productDAO.searchProducts(keyword, currentCategoryId, false);
            updatePaginationControls(true);
        }

        productList.setAll(list);
        tblProducts.setItems(productList);
    }

    private void updatePaginationControls(boolean isSearching) {
        if (isSearching) {
            lblPageInfo.setText("Kết quả tìm kiếm");
            btnPrev.setDisable(true);
            btnNext.setDisable(true);
        } else {
            lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
            btnPrev.setDisable(currentPage == 1);
            btnNext.setDisable(currentPage == totalPages);
        }
    }

    // --- EVENTS ---

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadData();
        }
    }

    public void refreshData() {
        calculatePagination();
        loadData();
    }

    // --- SETUP TABLE (Cấu hình hiển thị cột) ---

    private void setupTable() {
        // 1. Tên món
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
            }
        });

        // 2. Danh mục
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colCategory.setStyle("-fx-alignment: CENTER; -fx-font-size: 13px;");

        // 3. Giá bán
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else {
                    setText(String.format("%,.0f đ", price));
                    setStyle("-fx-font-weight: 900; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
                    setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                }
            }
        });

        // 4. Ảnh minh họa
        colImage.setStyle("-fx-alignment: CENTER;");
        colImage.setCellFactory(tc -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(45);
                imageView.setFitHeight(45);
                imageView.setPreserveRatio(false);
                Rectangle clip = new Rectangle(45, 45);
                clip.setArcWidth(12);
                clip.setArcHeight(12);
                imageView.setClip(clip);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Product p = getTableView().getItems().get(getIndex());
                    imageView.setImage(loadImage(p.getImagePath()));
                    setGraphic(imageView);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // 5. Trạng thái
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Product p = getTableView().getItems().get(getIndex());
                    Label lbl = new Label(p.isActive() ? "Đang bán" : "Đã ẩn");
                    String style = p.isActive() ? "-fx-background-color: #dcfce7; -fx-text-fill: #166534;" : "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
                    lbl.setStyle(style + "-fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 11px;");
                    setGraphic(lbl);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // 6. Thao tác (Icons)
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(8, btnToggle, btnEdit, btnDelete);

            {
                String btnStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 50%; " +
                        "-fx-cursor: hand; -fx-padding: 0; " +
                        "-fx-min-width: 30px; -fx-min-height: 30px; " +
                        "-fx-max-width: 30px; -fx-max-height: 30px;";

                btnToggle.setStyle(btnStyle);
                btnEdit.setStyle(btnStyle);
                btnDelete.setStyle(btnStyle);

                setupHover(btnToggle, "#f1f5f9");
                setupHover(btnEdit, "#eff6ff");
                setupHover(btnDelete, "#fef2f2");

                btnToggle.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    productDAO.toggleStatus(p.getId(), !p.isActive());
                    refreshData();
                });

                btnEdit.setOnAction(e -> handleOpenEditModal(getTableView().getItems().get(getIndex())));

                btnDelete.setOnAction(e -> {
                    if(confirmDelete()) {
                        productDAO.deleteProduct(getTableView().getItems().get(getIndex()).getId());
                        refreshData();
                    }
                });

                pane.setAlignment(javafx.geometry.Pos.CENTER);
            }

            private void setupHover(Button btn, String color) {
                String base = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 50%; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;";
                String hover = "-fx-background-color: " + color + "; -fx-border-color: #cbd5e1; -fx-border-radius: 50%; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;";
                btn.setOnMouseEntered(e -> btn.setStyle(hover));
                btn.setOnMouseExited(e -> btn.setStyle(base));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product p = getTableView().getItems().get(getIndex());
                    if (p.isActive()) {
                        btnToggle.setGraphic(createSVG(SVG_VISIBLE, "#64748b"));
                        btnToggle.setTooltip(new Tooltip("Ẩn món này"));
                    } else {
                        btnToggle.setGraphic(createSVG(SVG_HIDDEN, "#94a3b8"));
                        btnToggle.setTooltip(new Tooltip("Hiện món này"));
                    }
                    btnEdit.setGraphic(createSVG(SVG_EDIT, "#3b82f6"));
                    btnDelete.setGraphic(createSVG(SVG_DELETE, "#ef4444"));
                    setGraphic(pane);
                }
            }
        });
        tblProducts.setItems(productList);
    }

    // --- HELPER METHODS ---

    private SVGPath createSVG(String content, String color) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.setStyle("-fx-fill: " + color + ";");
        svg.setScaleX(0.7);
        svg.setScaleY(0.7);
        return svg;
    }

    private Image loadImage(String path) {
        if (path == null || path.trim().isEmpty()) return getDefaultImage();
        try {
            if (path.startsWith("http")) return new Image(path, true);
            String resourcePath = "/images/" + path;
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) return new Image(is);
        } catch (Exception e) {}
        return getDefaultImage();
    }

    private Image getDefaultImage() {
        try { return new Image(getClass().getResourceAsStream("/images/cafe-den.png")); }
        catch (Exception e) { return null; }
    }

    // --- MODAL & ALERTS ---

    @FXML private void handleOpenAddModal() { openModal(null); }
    private void handleOpenEditModal(Product p) { openModal(p); }

    private void openModal(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/ProductForm.fxml"));
            Parent root = loader.load();
            ProductFormController controller = loader.getController();
            controller.setProductData(product);
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa món này không?");
        return alert.showAndWait().get() == ButtonType.OK;
    }
}