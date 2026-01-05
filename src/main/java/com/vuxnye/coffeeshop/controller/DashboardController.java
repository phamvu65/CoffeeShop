package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.ReceiptDAO;
import com.vuxnye.coffeeshop.model.Receipt;
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

public class DashboardController {

    @FXML private Label lblRevenue;
    @FXML private Label lblOrders;
    @FXML private Label lblAvgOrder;
    @FXML private AreaChart<String, Number> areaChart;

    private ReceiptDAO receiptDAO = new ReceiptDAO();

    @FXML
    public void initialize() {
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

        for (int i = 6; i >= 0; i--) {
            revenueMap.put(today.minusDays(i), 0.0);
        }

        for (Receipt r : allReceipts) {
            if (r.getCreatedAt() != null) {
                // [ĐÃ SỬA] Thêm .toLocalDateTime() trước .toLocalDate()
                LocalDate date = r.getCreatedAt().toLocalDateTime().toLocalDate();

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

        // Style màu cho đường biểu đồ
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: #4A90E2; -fx-stroke-width: 3px;");
        }
    }
}