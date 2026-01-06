package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.ReceiptDAO;
import com.vuxnye.coffeeshop.model.Receipt;
import com.vuxnye.coffeeshop.util.Refreshable; // [1] Import Interface
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DashboardController implements Refreshable { // [2] Implements Interface

    @FXML private Label lblRevenue;
    @FXML private Label lblOrders;
    @FXML private Label lblAvgOrder;
    @FXML private AreaChart<String, Number> areaChart;

    private final ReceiptDAO receiptDAO = new ReceiptDAO();

    @FXML
    public void initialize() {
        // Thay vì gọi trực tiếp load..., ta gọi qua refreshData
        // để logic được thống nhất.
        refreshData();
    }

    // [3] Override hàm refreshData từ Interface Refreshable
    @Override
    public void refreshData() {
        // Tắt animation để tránh lỗi hiển thị khi reload nhanh
        if (areaChart != null) areaChart.setAnimated(false);

        loadDailyStats();
        loadWeeklyChart();
    }

    private void loadDailyStats() {
        List<Receipt> allReceipts = receiptDAO.getAllReceipts();

        LocalDate today = LocalDate.now();

        List<Receipt> todayReceipts = allReceipts.stream()
                .filter(r -> r.getCreatedAt() != null &&
                        r.getCreatedAt().toLocalDateTime().toLocalDate().equals(today))
                .collect(Collectors.toList());

        double dailyRevenue = todayReceipts.stream().mapToDouble(Receipt::getTotalPrice).sum();
        int totalOrders = todayReceipts.size();
        double avgOrder = totalOrders > 0 ? dailyRevenue / totalOrders : 0;

        lblRevenue.setText(String.format("%,.0f đ", dailyRevenue));
        lblOrders.setText(String.valueOf(totalOrders));
        lblAvgOrder.setText(String.format("%,.0f đ", avgOrder));
    }

    private void loadWeeklyChart() {
        List<Receipt> allReceipts = receiptDAO.getAllReceipts();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        Map<LocalDate, Double> revenueMap = new TreeMap<>();
        LocalDate today = LocalDate.now();

        // Tạo khung dữ liệu 7 ngày gần nhất (để ngày nào không bán cũng hiện số 0)
        for (int i = 6; i >= 0; i--) {
            revenueMap.put(today.minusDays(i), 0.0);
        }

        // Đổ dữ liệu thực tế vào Map
        for (Receipt r : allReceipts) {
            if (r.getCreatedAt() != null) {
                LocalDate date = r.getCreatedAt().toLocalDateTime().toLocalDate();

                // Chỉ cộng dồn nếu ngày đó nằm trong 7 ngày gần nhất
                if (revenueMap.containsKey(date)) {
                    revenueMap.put(date, revenueMap.get(date) + r.getTotalPrice());
                }
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<LocalDate, Double> entry : revenueMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().format(formatter), entry.getValue()));
        }

        areaChart.getData().clear();
        areaChart.getData().add(series);

        // Style màu cho đường biểu đồ (Chỉ chỉnh được sau khi add vào chart)
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: #4A90E2; -fx-stroke-width: 3px;");
        }
    }
}