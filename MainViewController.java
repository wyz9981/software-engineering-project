package com.book.keeping.controller;

import com.book.keeping.model.Transaction;
import com.book.keeping.util.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 主视图控制器类，处理FXML主界面的用户交互和业务逻辑
 */
public class MainViewController implements Initializable {
    // 交易记录列表
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    
    // 表格及其列
    @FXML
    private TableView<Transaction> transactionTable;
    
    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Transaction, String> descriptionColumn;
    
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    
    @FXML
    private TableColumn<Transaction, String> categoryColumn;
    
    @FXML
    private TableColumn<Transaction, String> sourceColumn;
    
    @FXML
    private TableColumn<Transaction, String> aiColumn;
    
    // 表单控件
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private TextField descriptionField;
    
    @FXML
    private TextField amountField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private ComboBox<String> sourceComboBox;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Label totalBalanceLabel;
    
    @FXML
    private MenuItem importMenuItem;
    
    @FXML
    private MenuItem dataAnalysisMenuItem;
    
    @FXML
    private MenuItem aiInsightMenuItem;
    
    @FXML
    private MenuItem chatMenuItem;
    
    @FXML
    private Button aiInsightButton;
    
    @FXML
    private Button chatButton;
    
    /**
     * 初始化控制器
     * @param location 位置
     * @param resources 资源
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格
        initializeTable();
        
        // 初始化表单
        initializeForm();
        
        // 计算总余额
        updateTotalBalance();
        
        // 初始化数据分析菜单项
        dataAnalysisMenuItem.setOnAction(event -> handleAnalysisAction());
        
        // 初始化AI洞察菜单项和按钮
        if (aiInsightMenuItem != null) {
            aiInsightMenuItem.setOnAction(event -> handleAiInsightAction());
        }
        
        if (aiInsightButton != null) {
            aiInsightButton.setOnAction(event -> handleAiInsightAction());
        }
        
        // 初始化聊天菜单项和按钮
        if (chatMenuItem != null) {
            chatMenuItem.setOnAction(event -> handleChatAction());
        }
        
        if (chatButton != null) {
            chatButton.setOnAction(event -> handleChatAction());
        }
        
        // 按日期排序
        sortTransactionsByDate();
    }
    
    /**
     * 更新总余额
     */
    private void updateTotalBalance() {
        double total = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        totalBalanceLabel.setText(String.format("$%.2f", total));
        
        // 根据金额正负设置样式
        totalBalanceLabel.getStyleClass().removeAll("income-cell", "expense-cell");
        if (total > 0) {
            totalBalanceLabel.getStyleClass().add("income-cell");
        } else if (total < 0) {
            totalBalanceLabel.getStyleClass().add("expense-cell");
        }
    }
    
    /**
     * 初始化交易表格
     */
    private void initializeTable() {
        // 设置表格属性
        transactionTable.setEditable(false);
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 日期列
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<Transaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)));
                }
                setAlignment(Pos.CENTER);
            }
        });
        
        // 描述列
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> {
            TableCell<Transaction, String> cell = new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });
        
        // 金额列
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("income-cell", "expense-cell");
                } else {
                    setText(String.format("%.2f", amount));
                    // 收入显示为橘色，支出显示为绿色（新设计）
                    getStyleClass().removeAll("income-cell", "expense-cell");
                    if (amount > 0) {
                        getStyleClass().add("income-cell");
                    } else if (amount < 0) {
                        getStyleClass().add("expense-cell");
                    }
                }
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        
        // 分类列
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setCellFactory(column -> {
            TableCell<Transaction, String> cell = new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        // 来源列
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceColumn.setCellFactory(column -> {
            TableCell<Transaction, String> cell = new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        // AI标记列
        aiColumn.setCellValueFactory(new PropertyValueFactory<>("aiGeneratedDisplay"));
        aiColumn.setCellFactory(column -> {
            TableCell<Transaction, String> cell = new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        // 设置行工厂以应用自定义样式
        transactionTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Transaction transaction = row.getItem();
                    showTransactionDetails(transaction);
                }
            });
            return row;
        });
        
        // 设置数据源
        transactionTable.setItems(transactions);
    }
    
    /**
     * 显示交易详情
     * @param transaction 交易记录
     */
    private void showTransactionDetails(Transaction transaction) {
        try {
            // 加载FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction-details-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // 获取控制器并设置数据
            TransactionDetailsController controller = loader.getController();
            controller.setTransaction(transaction);
            
            // 创建对话框
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Transaction Details");
            dialog.setDialogPane(dialogPane);
            
            // 显示对话框
            dialog.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Cannot load transaction details dialog: " + e.getMessage());
        }
    }
    
    /**
     * 初始化表单控件
     */
    private void initializeForm() {
        // 设置日期选择器格式
        datePicker.setValue(LocalDate.now());
        datePicker.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        
        // 设置分类下拉框
        categoryComboBox.setItems(FXCollections.observableArrayList(Constants.CATEGORIES));
        categoryComboBox.setValue("Uncategorized");
        
        // 设置来源下拉框
        sourceComboBox.setItems(FXCollections.observableArrayList(Constants.SOURCES));
        sourceComboBox.setValue(Constants.SOURCES.get(0));
    }
    
    /**
     * 处理添加按钮事件
     */
    @FXML
    private void handleAddAction() {
        // 验证输入
        if (!validateInput()) {
            return;
        }
        
        // 创建新交易记录
        try {
            LocalDate date = datePicker.getValue();
            String description = descriptionField.getText().trim();
            Double amount = Double.parseDouble(amountField.getText().trim());
            String category = categoryComboBox.getValue();
            String source = sourceComboBox.getValue();
            
            Transaction transaction = new Transaction(date, description, amount, category, source);
            
            // 添加到数据列表
            transactions.add(transaction);
            
            // 按日期排序
            sortTransactionsByDate();
            
            // 更新总余额
            updateTotalBalance();
            
            // 清除表单
            handleClearAction();
            
            // 提示添加成功
            showAlert(Alert.AlertType.INFORMATION, "Success", "Transaction added successfully!");
            
            // 滚动到最新添加的记录
            transactionTable.scrollTo(transaction);
            transactionTable.getSelectionModel().select(transaction);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid amount format. Please enter a valid number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Add Failed", "Error adding transaction: " + e.getMessage());
        }
    }
    
    /**
     * 处理清除按钮事件
     */
    @FXML
    private void handleClearAction() {
        datePicker.setValue(LocalDate.now());
        descriptionField.clear();
        amountField.clear();
        categoryComboBox.setValue("Uncategorized");
        sourceComboBox.setValue(Constants.SOURCES.get(0));
    }
    
    /**
     * 处理退出菜单项事件
     */
    @FXML
    private void handleExitAction() {
        System.exit(0);
    }
    
    /**
     * 验证表单输入
     * @return 验证结果
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (datePicker.getValue() == null) {
            errorMessage.append("- Please select a date\n");
        }
        
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            errorMessage.append("- Please enter a description\n");
        }
        
        String amountText = amountField.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            errorMessage.append("- Please enter an amount\n");
        } else {
            try {
                Double.parseDouble(amountText.trim());
            } catch (NumberFormatException e) {
                errorMessage.append("- Invalid amount format. Please enter a valid number\n");
            }
        }
        
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("- Please select a category\n");
        }
        
        if (sourceComboBox.getValue() == null) {
            errorMessage.append("- Please select a source\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Input Validation", "Please correct the following issues:\n" + errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * 显示提示对话框
     * @param alertType 对话框类型
     * @param title 标题
     * @param message 消息内容
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 设置按钮文本为英文
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        
        alert.showAndWait();
    }
    
    /**
     * 获取交易记录列表
     * @return 交易记录列表
     */
    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * 处理导入操作
     */
    @FXML
    private void handleImportAction() {
        try {
            // 加载导入对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/import-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // 创建对话框
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Import Transactions");
            dialog.setDialogPane(dialogPane);
            
            // 获取控制器
            ImportDialogController controller = loader.getController();
            
            // 显示对话框并等待用户操作
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData() == ButtonType.OK.getButtonData()) {
                    // 获取导入的数据
                    List<Transaction> importedTransactions = controller.getPreviewData();
                    
                    // 如果选择合并数据，则添加到现有数据中
                    if (controller.isMergeData()) {
                        transactions.addAll(importedTransactions);
                    } else {
                        // 否则替换现有数据
                        transactions.clear();
                        transactions.addAll(importedTransactions);
                    }
                    
                    // 更新总余额
                    updateTotalBalance();
                    
                    // 显示导入结果
                    showAlert(AlertType.INFORMATION, "Import Complete", 
                        String.format("Successfully imported %d transactions.", importedTransactions.size()));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Import Error", "Failed to load import dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleDataAnalysisAction() {
        showAlert(Alert.AlertType.INFORMATION, "Data Analysis", 
            "Data analysis feature is coming soon. This will include:\n" +
            "- Category-wise expense analysis\n" +
            "- Monthly income vs expense comparison\n" +
            "- Trend analysis\n" +
            "- Custom report generation");
    }

    @FXML
    private void handleAboutAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Personal Finance Manager");
        alert.setContentText(
            "Version: 1.0.0\n" +
            "Author: Bookkeeping Team\n" +
            "Description: A simple personal financial management tool for recording and tracking daily income and expenditure.\n\n" +
            "Main Features:\n" +
            "- Record daily income and expenditure\n" +
            "- Category management\n" +
            "- Data import/export\n" +
            "- Balance statistics\n" +
            "- Data analysis\n" +
            "- AI Insights powered by DeepSeek"
        );
        alert.showAndWait();
    }

    private void handleAnalysisAction() {
        try {
            // 加载数据分析视图
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/data-analysis-view.fxml"));
            Parent root = loader.load();
            
            // 获取控制器并设置数据
            DataAnalysisController controller = loader.getController();
            controller.setTransactions(transactions);
            
            // 创建新窗口
            Stage stage = new Stage();
            stage.setTitle("Data Analysis");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(transactionTable.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showError("Failed to open analysis view", e.getMessage());
        }
    }

    /**
     * 处理AI洞察按钮点击事件
     */
    @FXML
    private void handleAiInsightAction() {
        if (transactions.isEmpty()) {
            showAlert(AlertType.WARNING, "AI Insight", "There is no transaction data available for analysis. Please add some transaction records first。");
            return;
        }
        
        try {
            // 加载AI洞察视图
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai-insight-view.fxml"));
            Parent root = loader.load();
            
            // 获取控制器并设置数据
            AiInsightController controller = loader.getController();
            controller.setTransactions(transactions);
            
            // 创建新窗口
            Stage stage = new Stage();
            stage.setTitle("AI intelligent Financial insight");
            stage.setScene(new Scene(root, 800, 700));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(transactionTable.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showError("The AI Insights page cannot be opened", e.getMessage());
        }
    }

    /**
     * 处理聊天按钮点击事件
     */
    @FXML
    private void handleChatAction() {
        if (transactions.isEmpty()) {
            showAlert(AlertType.WARNING, "AI Financial Assistant", "There is no transaction data available for analysis. Please add some transaction records first so that the AI financial assistant can provide you with more accurate financial advice.");
            return;
        }
        
        try {
            // 加载聊天视图
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat-view.fxml"));
            Parent root = loader.load();
            
            // 获取控制器并设置数据
            ChatViewController controller = loader.getController();
            controller.setTransactions(transactions);
            
            // 创建新窗口
            Stage stage = new Stage();
            stage.setTitle("AI Financial Assistant");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(transactionTable.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            showError("The chat page cannot be opened", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * 按日期排序交易记录
     */
    private void sortTransactionsByDate() {
        FXCollections.sort(transactions, Comparator.comparing(Transaction::getDate).reversed());
    }
} 