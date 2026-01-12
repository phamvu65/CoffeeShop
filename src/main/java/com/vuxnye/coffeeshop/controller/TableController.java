package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.TableDAO;
import com.vuxnye.coffeeshop.model.Table;
import com.vuxnye.coffeeshop.util.Refreshable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.stream.Collectors;

public class TableController implements Refreshable {

    @FXML private FlowPane flowTables;
    @FXML private Button btnAll, btnEmpty, btnServing; // Đã bỏ btnReserved

    private TableDAO tableDAO = new TableDAO();
    private List<Table> allTables;
    private String currentFilter = "ALL";

    // SVG Icons
    private final String ICON_USER = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        allTables = tableDAO.getAllTables();
        renderTables();
    }

    private void renderTables() {
        flowTables.getChildren().clear();

        List<Table> filtered = allTables.stream()
                .filter(t -> currentFilter.equals("ALL") || t.getStatus().equals(currentFilter))
                .collect(Collectors.toList());

        for (Table t : filtered) {
            flowTables.getChildren().add(createTableCard(t));
        }

        updateFilterButtons();
    }

    private VBox createTableCard(Table t) {
        VBox card = new VBox();
        card.setPrefSize(200, 200); // Card vuông vắn hơn
        card.setAlignment(Pos.CENTER);
        card.setSpacing(15);
        card.getStyleClass().add("table-card"); // Class gốc (màu trắng)

        String iconPath;
        String statusText;
        String textColor;

        // Xử lý giao diện theo trạng thái
        if ("SERVING".equals(t.getStatus())) {
            card.getStyleClass().add("table-serving"); // Thêm class màu cam nền
            iconPath = ICON_USER;
            statusText = "Đang dùng";
            textColor = "#C2410C"; // Màu chữ cam đậm
        } else {
            // EMPTY
            iconPath = ICON_USER;
            statusText = "Bàn trống";
            textColor = "#64748B"; // Màu chữ xám
        }


        // Icon Box (Hình tròn chứa icon)
        VBox iconBox = new VBox();
        iconBox.setPrefSize(70, 70);
        iconBox.setMaxSize(70, 70);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.getStyleClass().add("table-icon-box");

        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);
        icon.setStyle("-fx-fill: " + textColor + ";");
        iconBox.getChildren().add(icon);

        // Tên bàn
        Label lblName = new Label(t.getName());
        lblName.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: " + ("SERVING".equals(t.getStatus()) ? "#7C2D12" : "#1E293B") + ";");

        // Trạng thái text
        Label lblStatus = new Label(statusText);
        lblStatus.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-transform: uppercase; -fx-text-fill: " + textColor + ";");


        card.getChildren().addAll(iconBox, lblName, lblStatus);

        // Sự kiện Click -> Đổi trạng thái (Trống <-> Đang dùng)
        card.setOnMouseClicked(e -> toggleStatus(t));

        return card;
    }

    private void toggleStatus(Table t) {
        // Logic mới: Chỉ xoay vòng Trống <-> Đang dùng (Bỏ Reserved)
        String nextStatus = t.getStatus().equals("EMPTY") ? "SERVING" : "EMPTY";

        tableDAO.updateStatus(t.getId(), nextStatus);
        loadData(); // Reload UI
    }

    // --- FILTER ACTIONS ---
    @FXML public void filterAll(ActionEvent e) { setFilter("ALL"); }
    @FXML public void filterEmpty(ActionEvent e) { setFilter("EMPTY"); }
    @FXML public void filterServing(ActionEvent e) { setFilter("SERVING"); }
    // Đã bỏ filterReserved

    private void setFilter(String status) {
        this.currentFilter = status;
        renderTables();
    }

    private void updateFilterButtons() {
        resetBtnStyle(btnAll);
        resetBtnStyle(btnEmpty);
        resetBtnStyle(btnServing);

        switch (currentFilter) {
            case "ALL": addActiveStyle(btnAll); break;
            case "EMPTY": addActiveStyle(btnEmpty); break;
            case "SERVING": addActiveStyle(btnServing); break;
        }
    }

    private void resetBtnStyle(Button btn) {
        if (btn != null) {
            btn.getStyleClass().remove("btn-filter-active");
            btn.getStyleClass().add("btn-filter");
        }
    }

    private void addActiveStyle(Button btn) {
        if (btn != null) btn.getStyleClass().add("btn-filter-active");
    }

    @Override
    public void refreshData() {
        loadData();
    }
}