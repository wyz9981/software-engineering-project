package com.book.keeping.controller;

import com.book.keeping.model.AiInsight;
import com.book.keeping.model.Transaction;
import com.book.keeping.service.DeepSeekService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * AI洞察视图控制器类
 * 处理AI洞察界面的业务逻辑
 */
public class AiInsightController implements Initializable {
    
    @FXML
    private Label lastUpdatedLabel;
    
    @FXML
    private Label overviewLabel;
    
    @FXML
    private Label monthlyBudgetLabel;
    
    @FXML
    private Label savingsGoalLabel;
    
    @FXML
    private ListView<String> suggestionsListView;
    
    @FXML
    private PieChart expenseCategoryChart;
    
    @FXML
    private VBox contentPane;
    
    @FXML
    private VBox loadingPane;
    
    private DeepSeekService deepSeekService;
    private ObservableList<Transaction> transactions;
    private AiInsight currentInsight;
    
    /**
     * 初始化控制器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deepSeekService = new DeepSeekService();
        
        // 初始设置加载状态为不可见
        loadingPane.setVisible(false);
        contentPane.setVisible(true);
    }
    
    /**
     * 设置交易数据
     * @param transactions 交易数据列表
     */
    public void setTransactions(ObservableList<Transaction> transactions) {
        this.transactions = transactions;
        
        // 设置完交易数据后自动生成洞察
        generateInsight();
    }
    
    /**
     * 处理刷新洞察按钮点击事件
     */
    @FXML
    private void handleRefreshInsight() {
        if (transactions == null || transactions.isEmpty()) {
            showError("Unable to generate insights", "There is no transaction data available for analysis. Please add some transaction records first.");
            return;
        }
        
        generateInsight();
    }
    
    /**
     * 处理关闭按钮点击事件
     */
    @FXML
    private void handleCloseAction() {
        // 获取窗口并关闭
        Stage stage = (Stage) overviewLabel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 生成AI洞察
     */
    private void generateInsight() {
        // 显示加载状态
        loadingPane.setVisible(true);
        contentPane.setVisible(false);
        
        Task<AiInsight> insightTask = new Task<AiInsight>() {
            @Override
            protected AiInsight call() throws Exception {
                try {
                    // 在实际应用中使用真实API
                    // return deepSeekService.generateInsight(transactions);
                    
                    // 开发阶段使用模拟数据
                    return deepSeekService.generateMockInsight(transactions);
                } catch (Exception e) {
                    throw e;
                }
            }
        };
        
        insightTask.setOnSucceeded(event -> {
            currentInsight = insightTask.getValue();
            updateUI(currentInsight);
            
            // 隐藏加载状态
            loadingPane.setVisible(false);
            contentPane.setVisible(true);
        });
        
        insightTask.setOnFailed(event -> {
            Throwable exception = insightTask.getException();
            showError("The generation of AI insights failed", "AI insights cannot be generated: " + exception.getMessage());
            
            // 隐藏加载状态
            loadingPane.setVisible(false);
            contentPane.setVisible(true);
        });
        
        new Thread(insightTask).start();
    }
    
    /**
     * 更新UI显示洞察结果
     * @param insight AI洞察对象
     */
    private void updateUI(AiInsight insight) {
        Platform.runLater(() -> {
            // 更新基本信息
            overviewLabel.setText(insight.getOverview());
            monthlyBudgetLabel.setText(String.format("¥%.2f", insight.getSuggestedMonthlyBudget()));
            savingsGoalLabel.setText(String.format("¥%.2f", insight.getSuggestedSavingsGoal()));
            
            // 更新成本削减建议列表
            suggestionsListView.setItems(
                    FXCollections.observableArrayList(insight.getCostReductionSuggestions())
            );
            
            // 更新最后更新时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            lastUpdatedLabel.setText("LastUpdated: " + insight.getGeneratedDate().format(formatter));
            
            // 更新支出分类饼图
            updateExpenseCategoryChart();
        });
    }
    
    /**
     * 更新支出分类饼图
     */
    private void updateExpenseCategoryChart() {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        // 计算每个类别的支出
        Map<String, Double> categoryExpenses = transactions.stream()
                .filter(t -> t.getAmount() < 0) // 只计算支出
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));
        
        // 创建饼图数据
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            pieChartData.add(new PieChart.Data(
                    entry.getKey() + " (¥" + String.format("%.2f", entry.getValue()) + ")",
                    entry.getValue()
            ));
        }
        
        expenseCategoryChart.setData(pieChartData);
    }
    
    /**
     * 显示错误对话框
     * @param title 标题
     * @param message 消息
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
} 