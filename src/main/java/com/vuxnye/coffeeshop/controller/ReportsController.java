package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.ReceiptDAO;
import com.vuxnye.coffeeshop.dao.ReportDAO;
import com.vuxnye.coffeeshop.model.Receipt;
import com.vuxnye.coffeeshop.util.Refreshable; // [1] Import Interface
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// [2] Implements Refreshable để Sidebar gọi được
public class ReportsController implements Refreshable {

    // --- CHART COMPONENTS ---
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    // --- FINANCIAL LABELS ---
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblCOGS;
    @FXML private Label lblGrossProfit;

    // --- HISTORY TABLE COMPONENTS (MỚI) ---
    @FXML private ComboBox<String> cbTimeFilter;
    @FXML private TableView<Receipt> tblHistory;
    @FXML private TableColumn<Receipt, Integer> colId;
    @FXML private TableColumn<Receipt, String> colDate;
    @FXML private TableColumn<Receipt, String> colCustomer;
    @FXML private TableColumn<Receipt, Double> colTotal;
    @FXML private TableColumn<Receipt, String> colPayment;

    // --- DAOs & DATA ---
    private final ReportDAO reportDAO = new ReportDAO();
    private final ReceiptDAO receiptDAO = new ReceiptDAO();

    // Bảng màu Pastel cho biểu đồ tròn
    private final String[] COLORS = {"#4A90E2", "#FFADAD", "#8E78FF", "#BDECB6", "#FFDCA2"};

    @FXML
    public void initialize() {
        // Tắt animation để tránh lỗi hiển thị cột/trục
        barChart.setAnimated(false);

        // Cấu hình Bảng Lịch sử
        setupHistoryTable();
        setupFilter();

        // [QUAN TRỌNG] Gọi refreshData() thay vì load từng cái lẻ tẻ
        refreshData();
    }

    // [3] Override hàm từ Interface
    @Override
    public void refreshData() {
        // Load lại Biểu đồ & Chỉ số
        loadPieChart();
        loadBarChart();
        loadFinancialStats();

        // Reset bộ lọc về "Hôm nay" và load lại bảng
        if (cbTimeFilter != null) {
            cbTimeFilter.getSelectionModel().selectFirst();
            loadHistoryData(LocalDate.now(), LocalDate.now());
        }
    }

    // =========================================================================
    // PHẦN 1: BIỂU ĐỒ & CHỈ SỐ TÀI CHÍNH
    // =========================================================================

    private void loadPieChart() {
        Map<String, Double> data = reportDAO.getRevenueByCategory();
        pieChart.getData().clear();

        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);

            String color = COLORS[i % COLORS.length];
            slice.getNode().setStyle("-fx-pie-color: " + color + ";");

            String msg = String.format("%s: %,.0f đ", entry.getKey(), entry.getValue());
            Tooltip.install(slice.getNode(), new Tooltip(msg));
            i++;
        }
    }

    private void loadBarChart() {
        Map<String, Integer> data = reportDAO.getTopSellingProducts();

        if (data.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top sản phẩm");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().clear();
        barChart.getData().add(series);

        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-bar-fill: #8E78FF;");
                String text = d.getXValue() + "\nĐã bán: " + d.getYValue() + " ly";
                Tooltip t = new Tooltip(text);
                t.setStyle("-fx-font-size: 14px;");
                Tooltip.install(d.getNode(), t);
            }
        }
    }

    private void loadFinancialStats() {
        double totalRevenue = reportDAO.getTotalRevenue();
        double cogs = totalRevenue * 0.35;
        double profit = totalRevenue * 0.65;

        lblTotalRevenue.setText(String.format("%,.0f đ", totalRevenue));
        lblCOGS.setText(String.format("%,.0f đ", cogs));
        lblGrossProfit.setText(String.format("%,.0f đ", profit));
    }

    // =========================================================================
    // PHẦN 2: LỊCH SỬ ĐƠN HÀNG (HISTORY TABLE)
    // =========================================================================

    private void setupHistoryTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        // Sử dụng Lambda để tránh lỗi Reflection
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate())
        );

        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f đ", price));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #166534; -fx-alignment: CENTER-RIGHT;");
                }
            }
        });
    }
    private void setupFilter() {
        cbTimeFilter.setItems(FXCollections.observableArrayList(
                "Hôm nay", "Hôm qua", "7 ngày qua", "Tháng này", "Tháng trước"
        ));
        cbTimeFilter.getSelectionModel().selectFirst();

        cbTimeFilter.setOnAction(e -> {
            String selected = cbTimeFilter.getValue();
            if (selected == null) return;

            LocalDate today = LocalDate.now();
            LocalDate from = today;
            LocalDate to = today;

            switch (selected) {
                case "Hôm nay": from = today; to = today; break;
                case "Hôm qua": from = today.minusDays(1); to = today.minusDays(1); break;
                case "7 ngày qua": from = today.minusDays(6); to = today; break;
                case "Tháng này":
                    from = today.withDayOfMonth(1);
                    to = today.withDayOfMonth(today.lengthOfMonth());
                    break;
                case "Tháng trước":
                    LocalDate lastMonth = today.minusMonths(1);
                    from = lastMonth.withDayOfMonth(1);
                    to = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                    break;
            }
            loadHistoryData(from, to);
        });
    }

    private void loadHistoryData(LocalDate from, LocalDate to) {
        List<Receipt> list = receiptDAO.getReceiptsByDate(from, to);
        tblHistory.setItems(FXCollections.observableArrayList(list));
    }
}