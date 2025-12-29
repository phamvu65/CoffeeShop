package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CategoryDAO;
import com.vuxnye.coffeeshop.dao.ProductDAO;
import com.vuxnye.coffeeshop.dao.ReceiptDAO;
import com.vuxnye.coffeeshop.dao.TableDAO;
import com.vuxnye.coffeeshop.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class POSController {

    @FXML private TextField txtSearch;
    @FXML private GridPane gridProducts;

    // --- BỘ LỌC & CHỌN BÀN ---
    @FXML private ComboBox<Category> cbCategory;
    @FXML private ComboBox<Table> cbOrderType; // Dùng Object Table

    // --- TABLE VIEW GIỎ HÀNG ---
    @FXML private TableView<CartItem> tblCart;
    @FXML private TableColumn<CartItem, String> colName;
    @FXML private TableColumn<CartItem, Void> colQty;
    @FXML private TableColumn<CartItem, Double> colPrice;
    @FXML private TableColumn<CartItem, Void> colDelete;

    // --- LABELS TỔNG TIỀN ---
    @FXML private Label lblSubtotal, lblTax, lblTotal, lblUser;

    // --- DAOs ---
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private ReceiptDAO receiptDAO = new ReceiptDAO();
    private TableDAO tableDAO = new TableDAO();

    // --- DATA ---
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private User currentUser;

    private int currentCategoryId = 0;
    private String currentKeyword = "";

    @FXML
    public void initialize() {
        setupTable();

        loadCategories();
        loadTables(); // Load bàn từ DB

        loadProducts(); // Load sản phẩm

        calculateTotals();

        // Sự kiện tìm kiếm
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            currentKeyword = newVal;
            loadProducts();
        });

        // Sự kiện chọn danh mục
        cbCategory.setOnAction(e -> {
            Category selected = cbCategory.getValue();
            currentCategoryId = (selected != null) ? selected.getId() : 0;
            loadProducts();
        });
    }

    // --- LOAD DỮ LIỆU ---

    private void loadCategories() {
        List<Category> list = categoryDAO.getAllCategories();
        list.add(0, new Category(0, "Tất cả"));
        cbCategory.setItems(FXCollections.observableArrayList(list));
        cbCategory.getSelectionModel().selectFirst();
    }

    private void loadTables() {
        List<Table> tables = tableDAO.getAllTables();

        // Thêm option "Mang về" (ID = 0)
        Table takeAway = new Table(0, "Mang về", "EMPTY");
        tables.add(0, takeAway);

        cbOrderType.setItems(FXCollections.observableArrayList(tables));
        cbOrderType.getSelectionModel().selectFirst();

        // Custom hiển thị tên bàn trong ComboBox
        cbOrderType.setConverter(new StringConverter<Table>() {
            @Override
            public String toString(Table t) {
                if (t == null) return null;
                if (t.getId() == 0) return t.getName(); // Mang về
                // Hiển thị tên bàn + trạng thái
                return t.getName() + (t.getStatus().equals("SERVING") ? " (Đang dùng)" : "");
            }

            @Override
            public Table fromString(String string) { return null; }
        });
    }

    private void loadProducts() {
        gridProducts.getChildren().clear();

        // QUAN TRỌNG: Gọi tham số 'true' để CHỈ HIỆN MÓN ĐANG BÁN
        List<Product> products = productDAO.searchProducts(currentKeyword, currentCategoryId, true);

        int column = 0;
        int row = 1;

        for (Product p : products) {
            VBox card = createProductCard(p);
            gridProducts.add(card, column++, row);
            if (column == 4) {
                column = 0;
                row++;
            }
        }
    }

    // --- CẤU HÌNH BẢNG GIỎ HÀNG ---
    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("total"));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText((empty || price == null) ? null : String.format("%,.0f", price));
            }
        });

        // Cột số lượng (+/-)
        colQty.setCellFactory(param -> new TableCell<>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Label lblQty = new Label();
            private final HBox pane = new HBox(5, btnMinus, lblQty, btnPlus);
            {
                pane.setAlignment(Pos.CENTER);
                String btnStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-min-width: 25px; -fx-cursor: hand;";
                btnMinus.setStyle(btnStyle);
                btnPlus.setStyle(btnStyle);
                lblQty.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-min-width: 20px; -fx-alignment: center;");

                btnMinus.setOnAction(e -> updateQuantity(getTableView().getItems().get(getIndex()), -1));
                btnPlus.setOnAction(e -> updateQuantity(getTableView().getItems().get(getIndex()), 1));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    lblQty.setText(String.valueOf(getTableView().getItems().get(getIndex()).getQuantity()));
                    setGraphic(pane);
                }
            }
        });

        // Cột xóa
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("X");
            {
                btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 10px; -fx-font-weight: bold;");
                btnDelete.setOnAction(e -> {
                    cartList.remove(getTableView().getItems().get(getIndex()));
                    calculateTotals();
                });
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });

        tblCart.setItems(cartList);
    }

    private void updateQuantity(CartItem item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) cartList.remove(item);
        else item.setQuantity(newQty);
        tblCart.refresh();
        calculateTotals();
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cartList) subtotal += item.getTotal();

        double tax = subtotal * 0.1;
        double total = subtotal + tax;

        if (lblSubtotal != null) lblSubtotal.setText(String.format("%,.0f đ", subtotal));
        if (lblTax != null) lblTax.setText(String.format("%,.0f đ", tax));
        if (lblTotal != null) lblTotal.setText(String.format("%,.0f đ", total));
    }

    public void setSession(User user) {
        this.currentUser = user;
        if (user != null) lblUser.setText(user.getFullname() != null ? user.getFullname() : user.getUsername());
    }

    // --- UI THẺ SẢN PHẨM ---
    private VBox createProductCard(Product p) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);

        ImageView img = new ImageView();
        try {
            String path = p.getImagePath();
            Image image;
            // Load ảnh thông minh (Online hoặc Local)
            if (path != null && path.startsWith("http")) {
                image = new Image(path, true);
            } else if (path != null && !path.isEmpty()) {
                String resourcePath = "/images/" + path;
                if (getClass().getResource(resourcePath) != null) {
                    image = new Image(getClass().getResourceAsStream(resourcePath));
                } else {
                    image = new Image(getClass().getResourceAsStream("/images/cafe-den.png"));
                }
            } else {
                image = new Image(getClass().getResourceAsStream("/images/cafe-den.png"));
            }
            img.setImage(image);
        } catch (Exception e) {
            // Fallback nếu lỗi
        }

        img.setFitHeight(140);
        img.setFitWidth(180);
        img.setPreserveRatio(true);

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        Label price = new Label(String.format("%,.0f đ", p.getPrice()));
        price.setStyle("-fx-text-fill: #4A90E2; -fx-font-weight: bold; -fx-font-size: 14px;");

        card.getChildren().addAll(img, name, price);
        card.setOnMouseClicked(e -> addToCart(p));
        return card;
    }

    private void addToCart(Product p) {
        for (CartItem item : cartList) {
            if (item.getProduct().getId() == p.getId()) {
                updateQuantity(item, 1);
                return;
            }
        }
        cartList.add(new CartItem(p, 1));
        calculateTotals();
    }

    // --- THANH TOÁN ---
    @FXML
    public void handleOpenCheckout() {
        if (cartList.isEmpty()) {
            showAlert("Giỏ hàng trống", "Vui lòng chọn món trước khi thanh toán!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/Checkout.fxml"));
            Parent root = loader.load();

            double subtotal = cartList.stream().mapToDouble(CartItem::getTotal).sum();

            CheckoutController controller = loader.getController();
            controller.setOrderData(subtotal, this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void onCheckoutCompleted(String method, Customer customer, double discount, double finalTotal) {
        double subtotal = cartList.stream().mapToDouble(CartItem::getTotal).sum();
        String dbMethod = method.equals("TIỀN MẶT") ? "CASH" : "TRANSFER";

        Table selectedTable = cbOrderType.getValue();

        boolean success = receiptDAO.createReceipt(cartList, subtotal, discount, finalTotal, dbMethod, customer, currentUser);

        if (success) {
            // Update trạng thái bàn
            if (selectedTable != null && selectedTable.getId() > 0) {
                if ("EMPTY".equals(selectedTable.getStatus())) {
                    tableDAO.updateStatus(selectedTable.getId(), "SERVING");
                    loadTables(); // Reload combo box

                    // Restore selection
                    for(Table t : cbOrderType.getItems()) {
                        if(t.getId() == selectedTable.getId()) {
                            cbOrderType.getSelectionModel().select(t);
                            break;
                        }
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Thanh toán thành công!");
            String tableName = (selectedTable == null || selectedTable.getId() == 0) ? "Mang về" : selectedTable.getName();
            alert.setContentText("Đơn hàng: " + tableName + "\nThực thu: " + String.format("%,.0f đ", finalTotal));
            alert.showAndWait();

            cartList.clear();
            calculateTotals();
        } else {
            showAlert("Lỗi", "Có lỗi xảy ra khi lưu hóa đơn.");
        }
    }

    @FXML
    public void handleLogout(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/Login.fxml"));
            Stage stage = (Stage) lblUser.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}