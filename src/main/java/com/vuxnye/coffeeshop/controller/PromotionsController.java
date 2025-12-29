package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.PromotionDAO;
import com.vuxnye.coffeeshop.model.Promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class PromotionsController {

    @FXML private TextField txtSearch;
    @FXML private TableView<Promotion> tblPromotions;

    // [QUAY VỀ] Tách riêng cột Tên và Mã
    @FXML private TableColumn<Promotion, String> colName, colCode;

    @FXML private TableColumn<Promotion, String> colType;
    @FXML private TableColumn<Promotion, Double> colValue;
    @FXML private TableColumn<Promotion, Void> colDuration, colStatus, colAction;

    private PromotionDAO promoDAO = new PromotionDAO();
    private ObservableList<Promotion> promoList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // SVG Icons
    private final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
    private final String SVG_DELETE = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";
    private final String SVG_PLAY = "M8 5v14l11-7z";
    private final String SVG_PAUSE = "M6 19h4V5H6v14zm8-14v14h4V5h-4z";

    @FXML
    public void initialize() {
        setupTable();
        refreshData();

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            promoList.setAll(promoDAO.searchPromotions(newVal));
        });
    }

    private void setupTable() {
        // 1. CỘT TÊN CHƯƠNG TRÌNH
        colName.setCellValueFactory(new PropertyValueFactory<>("description"));
        colName.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
                setWrapText(true);
            }
        });

        // 2. CỘT MÃ CODE (Màu đỏ hồng, căn giữa)
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCode.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #E11D48; -fx-font-size: 11px; -fx-background-color: #FFE4E6; -fx-padding: 2 6; -fx-background-radius: 4;");
                    setGraphic(lbl);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // 3. LOẠI
        colType.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        colType.setStyle("-fx-alignment: CENTER; -fx-font-size: 13px;");

        // 4. GIÁ TRỊ
        colValue.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else {
                    Promotion p = getTableView().getItems().get(getIndex());
                    String val = "PERCENT".equals(p.getType()) ? String.format("%.0f%%", p.getValue()) : String.format("%,.0f đ", p.getValue());
                    setText(val);
                    setStyle("-fx-font-weight: 900; -fx-text-fill: #1e293b; -fx-alignment: CENTER-RIGHT; -fx-font-size: 13px;");
                }
            }
        });

        // 5. THỜI HẠN
        colDuration.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else {
                    Promotion p = getTableView().getItems().get(getIndex());
                    String start = p.getStartDate() != null ? dateFormat.format(p.getStartDate()) : "?";
                    String end = p.getEndDate() != null ? dateFormat.format(p.getEndDate()) : "?";
                    setText(start + " - " + end);
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
                }
            }
        });

        // 6. TRẠNG THÁI
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Promotion p = getTableView().getItems().get(getIndex());
                    boolean expired = p.getEndDate() != null && p.getEndDate().getTime() < System.currentTimeMillis();
                    String text = expired ? "Hết hạn" : (p.isActive() ? "Đang chạy" : "Tạm dừng");

                    String style = expired ? "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444;" :
                            (p.isActive() ? "-fx-background-color: #dcfce7; -fx-text-fill: #166534;" :
                                    "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;");

                    Label lbl = new Label(text);
                    lbl.setStyle(style + "-fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 6; -fx-font-size: 10px;");
                    setGraphic(lbl);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // 7. CỘT THAO TÁC (Giữ nguyên nút nhỏ 30px)
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
                    Promotion p = getTableView().getItems().get(getIndex());
                    promoDAO.toggleStatus(p.getId(), !p.isActive());
                    refreshData();
                });
                btnEdit.setOnAction(e -> openModal(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> {
                    if(confirmDelete()) {
                        promoDAO.deletePromotion(getTableView().getItems().get(getIndex()).getId());
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
                    Promotion p = getTableView().getItems().get(getIndex());
                    btnToggle.setGraphic(createSVG(p.isActive() ? SVG_PAUSE : SVG_PLAY, "#64748b"));
                    btnToggle.setTooltip(new Tooltip(p.isActive() ? "Tạm dừng" : "Kích hoạt"));
                    btnEdit.setGraphic(createSVG(SVG_EDIT, "#3b82f6"));
                    btnEdit.setTooltip(new Tooltip("Chỉnh sửa"));
                    btnDelete.setGraphic(createSVG(SVG_DELETE, "#ef4444"));
                    btnDelete.setTooltip(new Tooltip("Xóa"));
                    setGraphic(pane);
                }
            }
        });

        tblPromotions.setItems(promoList);
    }

    private SVGPath createSVG(String content, String color) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.setStyle("-fx-fill: " + color + ";");
        svg.setScaleX(0.7);
        svg.setScaleY(0.7);
        return svg;
    }

    public void refreshData() {
        promoList.setAll(promoDAO.getAllPromotions());
    }

    @FXML private void handleOpenAddModal() { openModal(null); }

    private void openModal(Promotion p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/PromotionForm.fxml"));
            Parent root = loader.load();
            PromotionFormController controller = loader.getController();
            controller.setPromoData(p);
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
        alert.setContentText("Bạn có chắc chắn muốn xóa khuyến mãi này?");
        return alert.showAndWait().get() == ButtonType.OK;
    }
}