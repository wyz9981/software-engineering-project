package com.book.keeping.controller;

import com.book.keeping.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析视图控制器
 */
public class DataAnalysisController implements Initializable {
    
    @FXML
    private BarChart<String, Number> monthlyChart;
    
    @FXML
    private PieChart categoryChart;
    
    @FXML
    private LineChart<String, Number> trendChart;
    
    @FXML
    private ComboBox<String> timeRangeComboBox;
    
    @FXML
    private Label totalIncomeLabel;
    
    @FXML
    private Label totalExpenseLabel;
    
    @FXML
    private Label netBalanceLabel;
    
    private ObservableList<Transaction> transactions;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    
    /**
     * 设置交易数据
     * @param transactions 交易数据列表
     */
    public void setTransactions(ObservableList<Transaction> transactions) {
        this.transactions = transactions;
        updateAnalysis();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化时间范围选项
        timeRangeComboBox.getItems().addAll(
            "Last 7 Days",
            "Last 30 Days",
            "Last 3 Months",
            "Last 6 Months",
            "Last Year",
            "All Time"
        );
        timeRangeComboBox.setValue("Last 30 Days");  // 设置默认值
        
        // 初始化图表
        initializeCharts();
    }
    
    /**
     * 初始化图表
     */
    private void initializeCharts() {
        // 初始化月度统计图表
        if (monthlyChart.getData().isEmpty()) {
            XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
            incomeSeries.setName("Income");
            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Expenses");
            monthlyChart.getData().addAll(incomeSeries, expenseSeries);
        }

        // 初始化分类统计图表
        categoryChart.setLegendSide(Side.RIGHT);

        // 初始化趋势图表
        if (trendChart.getData().isEmpty()) {
            XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
            balanceSeries.setName("Balance");
            trendChart.getData().add(balanceSeries);
        }
        
        // 初始化时间范围选择器
        timeRangeComboBox.getSelectionModel().select("Last 30 Days");
        timeRangeComboBox.setOnAction(event -> updateAnalysis());
    }
    
    @FXML
    private void handleUpdateAnalysis() {
        updateAnalysis();
    }
    
    /**
     * 更新分析数据
     */
    private void updateAnalysis() {
        if (transactions == null) return;

        LocalDate startDate = getStartDate(timeRangeComboBox.getValue());
        LocalDate endDate = LocalDate.now();

        // 过滤指定日期范围内的交易
        List<Transaction> filteredTransactions = transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .collect(Collectors.toList());

        // 更新月度统计
        updateMonthlyStatistics(filteredTransactions);

        // 更新分类统计
        updateCategoryStatistics(filteredTransactions);

        // 更新趋势分析
        updateTrendAnalysis(filteredTransactions);

        // 更新统计信息
        updateSummaryStatistics(filteredTransactions);
    }
    
    /**
     * 获取开始日期
     * @param timeRange 时间范围
     * @return 开始日期
     */
    private LocalDate getStartDate(String timeRange) {
        LocalDate now = LocalDate.now();
        switch (timeRange) {
            case "Last 7 Days":
                return now.minusDays(7);
            case "Last 30 Days":
                return now.minusDays(30);
            case "Last 3 Months":
                return now.minusMonths(3);
            case "Last 6 Months":
                return now.minusMonths(6);
            case "Last Year":
                return now.minusYears(1);
            default:
                return LocalDate.MIN;
        }
    }
    
    /**
     * 更新月度统计图表
     * @param transactions 交易数据
     */
    private void updateMonthlyStatistics(List<Transaction> transactions) {
        // 按月份分组统计收入和支出
        Map<String, Double> monthlyIncome = new TreeMap<>();
        Map<String, Double> monthlyExpense = new TreeMap<>();

        for (Transaction t : transactions) {
            String month = t.getDate().format(DateTimeFormatter.ofPattern("MMM"));
            double amount = t.getAmount();
            
            if (amount > 0) {
                monthlyIncome.merge(month, amount, Double::sum);
            } else {
                monthlyExpense.merge(month, Math.abs(amount), Double::sum);
            }
        }

        // 更新图表数据
        XYChart.Series<String, Number> incomeSeries = monthlyChart.getData().get(0);
        XYChart.Series<String, Number> expenseSeries = monthlyChart.getData().get(1);
        
        incomeSeries.getData().clear();
        expenseSeries.getData().clear();

        for (String month : monthlyIncome.keySet()) {
            incomeSeries.getData().add(new XYChart.Data<>(month, monthlyIncome.get(month)));
            expenseSeries.getData().add(new XYChart.Data<>(month, 
                monthlyExpense.getOrDefault(month, 0.0)));
        }
    }

    /**
     * 更新分类统计图表
     * @param transactions 交易数据
     */
    private void updateCategoryStatistics(List<Transaction> transactions) {
        // 按类别统计支出
        Map<String, Double> categoryAmounts = new HashMap<>();
        
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) {  // 支出
                categoryAmounts.merge(t.getCategory(), Math.abs(t.getAmount()), Double::sum);
            }
        }

        // 更新饼图数据
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryAmounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        categoryChart.setData(pieChartData);
    }

    /**
     * 更新趋势分析图表
     * @param transactions 交易数据
     */
    private void updateTrendAnalysis(List<Transaction> transactions) {
        // 按日期排序
        transactions.sort(Comparator.comparing(Transaction::getDate));

        // 计算每日余额
        Map<String, Double> dailyBalance = new TreeMap<>();
        double balance = 0.0;

        for (Transaction t : transactions) {
            String date = t.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            balance += t.getAmount();  // 直接累加金额
            dailyBalance.put(date, balance);
        }

        // 更新趋势图表
        XYChart.Series<String, Number> balanceSeries = trendChart.getData().get(0);
        balanceSeries.getData().clear();

        for (Map.Entry<String, Double> entry : dailyBalance.entrySet()) {
            balanceSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * 更新统计信息
     * @param transactions 交易数据
     */
    private void updateSummaryStatistics(List<Transaction> transactions) {
        double totalIncome = transactions.stream()
            .filter(t -> t.getAmount() > 0)  // 收入
            .mapToDouble(Transaction::getAmount)
            .sum();

        double totalExpense = transactions.stream()
            .filter(t -> t.getAmount() < 0)  // 支出
            .mapToDouble(t -> Math.abs(t.getAmount()))  // 取绝对值
            .sum();

        double netBalance = totalIncome - totalExpense;

        totalIncomeLabel.setText(String.format("¥%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("¥%.2f", totalExpense));
        netBalanceLabel.setText(String.format("¥%.2f", netBalance));
    }
} 