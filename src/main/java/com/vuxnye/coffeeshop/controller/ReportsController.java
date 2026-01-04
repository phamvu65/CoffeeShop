package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.ReportDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Map;

public class ReportsController {

    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblCOGS;
    @FXML private Label lblGrossProfit;

    private ReportDAO reportDAO = new ReportDAO();

    // Bảng màu Pastel
    private final String[] COLORS = {"#4A90E2", "#FFADAD", "#8E78FF", "#BDECB6", "#FFDCA2"};

    @FXML
    public void initialize() {
        loadPieChart();
        loadBarChart();
        loadFinancialStats();
    }

    private void loadPieChart() {
        Map<String, Double> data = reportDAO.getRevenueByCategory();
        pieChart.getData().clear();

        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);

            // Set màu sắc cho từng miếng
            String color = COLORS[i % COLORS.length];
            slice.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }

    private void loadBarChart() {
        Map<String, Integer> data = reportDAO.getTopSellingProducts();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng bán");

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            XYChart.Data<String, Number> barData = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(barData);
        }

        barChart.getData().clear();
        barChart.getData().add(series);

        // Tô màu tím cho cột
        for (XYChart.Data<String, Number> d : series.getData()) {
            if(d.getNode() != null) {
                d.getNode().setStyle("-fx-bar-fill: #8E78FF;");
            }
        }
    }

    private void loadFinancialStats() {
        double totalRevenue = reportDAO.getTotalRevenue();
        double cogs = totalRevenue * 0.35; // Giả định chi phí vốn 35%
        double profit = totalRevenue * 0.65; // Lợi nhuận 65%

        lblTotalRevenue.setText(String.format("%,.0f đ", totalRevenue));
        lblCOGS.setText(String.format("%,.0f đ", cogs));
        lblGrossProfit.setText(String.format("%,.0f đ", profit));
    }
}