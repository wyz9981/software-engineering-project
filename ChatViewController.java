package com.book.keeping.controller;

import com.book.keeping.model.ChatMessage;
import com.book.keeping.model.ChatResponse;
import com.book.keeping.model.ChatSession;
import com.book.keeping.model.Transaction;
import com.book.keeping.service.DeepSeekService;
import com.book.keeping.ui.ChatMessageCell;
import com.book.keeping.ui.LoadingIndicator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天视图控制器类
 * 负责管理聊天界面的交互逻辑和与DeepSeekService的通信
 */
public class ChatViewController implements Initializable {

    @FXML
    private StackPane rootPane;
    
    @FXML
    private ListView<ChatMessage> chatListView;
    
    @FXML
    private ScrollPane chatScrollPane;
    
    @FXML
    private TextField messageField;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private StackPane loadingPane;
    
    @FXML
    private Label loadingLabel;
    
    @FXML
    private StackPane progressIndicatorWrapper;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button stopButton;
    
    private DeepSeekService deepSeekService;
    private List<Transaction> transactions;
    private ChatSession chatSession;
    private LoadingIndicator loadingIndicator;
    private CompletableFuture<Void> currentRequest;
    
    /**
     * 初始化控制器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化DeepSeekService
        deepSeekService = new DeepSeekService();
        
        // 初始化聊天会话
        chatSession = new ChatSession();
        
        // 配置ListView使用自定义单元格
        chatListView.setCellFactory(param -> new ChatMessageCell());
        
        // 禁用水平滚动条
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // 初始化加载指示器
        loadingIndicator = new LoadingIndicator();
        progressIndicatorWrapper.getChildren().add(loadingIndicator);
        
        // 确保加载面板不会阻挡停止按钮的点击事件
        if (loadingPane != null) {
            // 设置加载面板的鼠标事件透明度
            loadingPane.setMouseTransparent(false);
            // 确保加载面板只覆盖聊天区域
            loadingPane.setPickOnBounds(false);
        }
        
        // 添加欢迎消息
        ChatMessage welcomeMessage = ChatMessage.createAiMessage(
                "Hello! I'm your personal financial assistant. I can assist you in analyzing your budget, savings strategies, spending patterns and investment suggestions. May I ask if there are any financial issues that I need to assist in answering?");
        chatSession.getMessages().add(welcomeMessage);
        chatListView.setItems(chatSession.getMessages());
        
        // 设置状态标签
        updateStatus("In leisure time");
        
        // 初始化停止按钮
        if (stopButton != null) {
            stopButton.setDisable(true);
            // 确保停止按钮样式类被正确应用
            if (!stopButton.getStyleClass().contains("stop-button")) {
                stopButton.getStyleClass().add("stop-button");
            }
        }
    }
    
    /**
     * 设置交易数据
     * @param transactions 用户的交易数据
     */
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    /**
     * 处理发送消息按钮事件
     */
    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // 清空输入框
        messageField.clear();
        
        // 处理用户消息并获取AI回复
        processUserMessage(message);
    }
    
    /**
     * 处理用户消息并获取AI回复
     * @param userMessage 用户输入的消息
     */
    private void processUserMessage(String userMessage) {
        // 先添加用户消息到聊天界面
        chatSession.addUserMessage(userMessage);
        scrollToBottom();
        
        // 显示加载状态
        setLoadingState(true, "Thinking...");
        
        // 禁用发送按钮，启用停止按钮
        sendButton.setDisable(true);
        if (stopButton != null) {
            stopButton.setDisable(false);
            // 确保停止按钮在最上层
            stopButton.toFront();
        }
        
        // 异步处理聊天请求
        currentRequest = CompletableFuture.supplyAsync(() -> {
            try {
                // 使用实际API而非模拟API
                boolean useMockApi = false; // 改为使用实际API
                
                String response;
                if (useMockApi) {
                    // 在后台线程中调用服务，不修改UI
                    String mockResponse = generateMockResponse(userMessage);
                    return mockResponse;
                } else {
                    // 实际API调用，不修改UI (因为用户消息已经添加)
                    // 传递空的用户消息，防止重复添加
                    ChatResponse responseObj = deepSeekService.chat(chatSession, userMessage, transactions);
                    
                    // 在JavaFX线程中处理AI响应
                    Platform.runLater(() -> {
                        // 只添加AI响应，因为用户消息已经添加
                        chatSession.addAiMessage(responseObj.getAiResponse());
                        scrollToBottom();
                    });
                    
                    return "";
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException || Thread.currentThread().isInterrupted()) {
                    return "The request has been cancelled.";
                }
                e.printStackTrace();
                return "Sorry, I encountered a problem when handling your request：" + e.getMessage();
            }
        }).thenAccept(response -> {
            // 在UI线程更新界面
            Platform.runLater(() -> {
                try {
                    // 隐藏加载状态
                    setLoadingState(false, "In leisure time");
                    
                    // 启用发送按钮，禁用停止按钮
                    sendButton.setDisable(false);
                    if (stopButton != null) {
                        stopButton.setDisable(true);
                    }
                    
                    // 如果是错误或取消响应，添加到聊天
                    if (!response.isEmpty()) {
                        chatSession.addAiMessage(response);
                        scrollToBottom();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setLoadingState(false, "error");
                    sendButton.setDisable(false);
                    if (stopButton != null) {
                        stopButton.setDisable(true);
                    }
                }
            });
        });
    }
    
    /**
     * 生成模拟回复（不涉及UI操作）
     */
    private String generateMockResponse(String userMessage) {
        try {
            // 模拟一些处理时间
            Thread.sleep(1000);
            
            // 这里仅调用服务层方法，不直接操作UI相关的ObservableList
            if (transactions == null || transactions.isEmpty()) {
                return "I noticed that you haven't recorded any transaction data yet. To obtain more personalized financial advice, please add some transaction records first. Meanwhile, I can answer general financial questions.";
            }
            
            String userMessageLower = userMessage.toLowerCase();
            
            // 分析用户消息内容，生成相应的回复
            if (userMessageLower.contains("预算") || userMessageLower.contains("budget")) {
                double avgExpense = calculateAverageMonthlyExpense();
                return String.format(
                        "基于您的交易记录，我建议您的月度预算控制在 ¥%.2f 左右。建议将预算分配到不同的支出类别，并定期跟踪您的支出情况。",
                        avgExpense * 0.9);
            } else if (userMessageLower.contains("储蓄") || userMessageLower.contains("saving")) {
                double avgIncome = calculateAverageMonthlyIncome();
                return String.format(
                        "理财专家通常建议将收入的20%%用于储蓄。根据您的收入情况，建议每月储蓄 ¥%.2f。您可以考虑设立紧急备用金、退休储蓄和短期储蓄目标。",
                        avgIncome * 0.2);
            } else if (userMessageLower.contains("投资") || userMessageLower.contains("invest")) {
                return "投资前，建议先确保您有足够的紧急备用金（通常为3-6个月的生活开支）。投资组合可以考虑股票、债券和基金的多元化配置，根据您的风险承受能力调整各类资产的比例。";
            } else if (userMessageLower.contains("消费") || userMessageLower.contains("支出") || userMessageLower.contains("expense")) {
                // 找出主要支出类别
                String expenseAnalysis = analyzeTopExpenses();
                if (expenseAnalysis.isEmpty()) {
                    return "您的交易记录中暂无足够的支出数据进行分析。建议您记录更多的日常支出，我才能提供更精准的消费分析。";
                } else {
                    return expenseAnalysis;
                }
            } else {
                // 默认回复
                return "作为您的财务顾问，我可以帮助您分析预算、储蓄策略、支出模式和投资建议。请告诉我您想了解哪方面的财务信息？";
            }
        } catch (Exception e) {
            return "Sorry, I encountered a problem when analyzing your data：" + e.getMessage();
        }
    }
    
    /**
     * 计算月平均收入（不涉及UI操作）
     */
    private double calculateAverageMonthlyIncome() {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }
        
        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        return totalIncome / Math.max(1, transactions.size() / 30.0);
    }
    
    /**
     * 计算月平均支出（不涉及UI操作）
     */
    private double calculateAverageMonthlyExpense() {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }
        
        double totalExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum());
        
        return totalExpense / Math.max(1, transactions.size() / 30.0);
    }
    
    /**
     * 分析主要支出类别（不涉及UI操作）
     */
    private String analyzeTopExpenses() {
        if (transactions == null || transactions.isEmpty()) {
            return "";
        }
        
        // 创建支出类别映射
        java.util.Map<String, Double> categoryExpenses = new java.util.HashMap<>();
        
        // 计算每个类别的总支出
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) {
                String category = t.getCategory();
                double expense = Math.abs(t.getAmount());
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + expense);
            }
        }
        
        // 如果没有支出数据，返回空字符串
        if (categoryExpenses.isEmpty()) {
            return "";
        }
        
        // 对类别进行排序，找出前三名
        java.util.List<java.util.Map.Entry<String, Double>> sortedExpenses = new java.util.ArrayList<>(categoryExpenses.entrySet());
        sortedExpenses.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));
        
        // 最多取前三名
        int limit = Math.min(3, sortedExpenses.size());
        StringBuilder result = new StringBuilder("Your main expenditure category is：");
        
        // 生成分析结果
        for (int i = 0; i < limit; i++) {
            java.util.Map.Entry<String, Double> entry = sortedExpenses.get(i);
            result.append(String.format("\n- %s: ¥%.2f", entry.getKey(), entry.getValue()));
        }
        
        result.append("\n\nIt is suggested to focus on these major expenditures and look for possible areas for savings.");
        return result.toString();
    }
    
    /**
     * 设置加载状态
     * @param isLoading 是否正在加载
     * @param status 状态文本
     */
    private void setLoadingState(boolean isLoading, String status) {
        // 确保在UI线程中执行
        if (Platform.isFxApplicationThread()) {
            // 如果已经在JavaFX应用线程中，直接更新UI
            updateLoadingState(isLoading, status);
        } else {
            // 如果不在JavaFX应用线程中，通过Platform.runLater更新UI
            Platform.runLater(() -> updateLoadingState(isLoading, status));
        }
    }
    
    /**
     * 实际更新加载状态的方法（总是在JavaFX线程中调用）
     */
    private void updateLoadingState(boolean isLoading, String status) {
        loadingPane.setVisible(isLoading);
        
        if (isLoading) {
            loadingIndicator.start();
            
            // 确保停止按钮在加载状态下可见且可用
            if (stopButton != null) {
                stopButton.setDisable(false);
                stopButton.toFront();
            }
        } else {
            loadingIndicator.stop();
            
            // 加载结束后，禁用停止按钮
            if (stopButton != null) {
                stopButton.setDisable(true);
            }
        }
        
        updateStatus(status);
    }
    
    /**
     * 更新状态标签
     * @param status 状态文本
     */
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * 滚动聊天窗口到底部
     */
    private void scrollToBottom() {
        chatListView.scrollTo(chatListView.getItems().size() - 1);
    }
    
    /**
     * 处理清空聊天按钮事件
     */
    @FXML
    private void handleClearChat() {
        chatSession.clearMessages();
        
        // 添加新的欢迎消息
        ChatMessage welcomeMessage = ChatMessage.createAiMessage(
                "The chat has been cleared. May I ask if there are any financial issues that I need to assist in answering？");
        chatSession.getMessages().add(welcomeMessage);
        
        // 滚动到底部
        scrollToBottom();
    }
    
    /**
     * 处理关闭按钮事件
     */
    @FXML
    private void handleCloseAction() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 处理停止按钮事件
     */
    @FXML
    private void handleStopRequest() {
        System.out.println("The stop button was clicked");
        
        if (currentRequest != null && !currentRequest.isDone()) {
            // 取消请求（带有中断参数设置为true）
            boolean cancelResult = currentRequest.cancel(true);
            System.out.println("The user cancelled the request and the result: " + cancelResult);
            
            // 立即更新UI状态
            setLoadingState(false, "cancelled");
            sendButton.setDisable(false);
            if (stopButton != null) {
                stopButton.setDisable(true);
            }
            
            // 添加取消消息
            chatSession.addAiMessage("The request has been cancelled.");
            scrollToBottom();
        } else {
            System.out.println("No ongoing requests can be cancelled");
        }
    }
} 