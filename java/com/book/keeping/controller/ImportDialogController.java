package com.book.keeping.controller;

import com.book.keeping.model.Transaction;
import com.book.keeping.util.CSVImporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 导入对话框控制器
 */
public class ImportDialogController {
    @FXML
    private TextField filePathField;
    
    @FXML
    private Button browseButton;
    
    @FXML
    private TableView<Transaction> previewTable;
    
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    
    @FXML
    private TableColumn<Transaction, String> descriptionColumn;
    
    @FXML
    private TableColumn<Transaction, String> amountColumn;
    
    @FXML
    private TableColumn<Transaction, String> categoryColumn;
    
    @FXML
    private TableColumn<Transaction, String> sourceColumn;
    
    @FXML
    private CheckBox skipHeaderCheckBox;
    
    @FXML
    private CheckBox mergeDataCheckBox;
    
    @FXML
    private TextArea formatInfoArea;
    
    @FXML
    private ButtonType importButton;
    
    @FXML
    private ButtonType cancelButton;
    
    private File selectedFile;
    private final ObservableList<Transaction> previewData = FXCollections.observableArrayList();
    
    /**
     * 初始化控制器
     */
    @FXML
    public void initialize() {
        // 初始化预览表格
        initializePreviewTable();
        
        // 设置表格占位符
        Label placeholder = new Label("No data to display. Please select a CSV file to preview.");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        previewTable.setPlaceholder(placeholder);
    }
    
    /**
     * 初始化预览表格
     */
    private void initializePreviewTable() {
        // 设置表格属性
        previewTable.setEditable(false);
        previewTable.setItems(previewData);
        
        // 设置列属性
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        
        amountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f", cellData.getValue().getAmount())));
        
        categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory()));
        
        sourceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSource()));
    }
    
    /**
     * 处理浏览按钮事件
     */
    @FXML
    private void handleBrowseAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        Window window = browseButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(window);
        
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            loadPreviewData();
        }
    }
    
    /**
     * 加载预览数据
     */
    private void loadPreviewData() {
        if (selectedFile == null) {
            return;
        }
        
        try {
            CSVImporter.ImportResult result = CSVImporter.importFromCSV(selectedFile.getAbsolutePath(), skipHeaderCheckBox.isSelected());
            
            // 更新预览数据
            previewData.clear();
            previewData.addAll(result.getTransactions());
            
            // 按日期排序
            sortTransactionsByDate();
            
            // 显示错误信息（如果有）
            if (result.hasErrors()) {
                showAlert(Alert.AlertType.WARNING, "Import Warnings", 
                    String.format("Found %d errors in the file. Some records may not be imported correctly.", 
                    result.getErrorCount()));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Import Error", 
                "Error loading preview data: " + e.getMessage());
        }
    }
    
    /**
     * 按日期排序交易记录
     */
    private void sortTransactionsByDate() {
        FXCollections.sort(previewData, Comparator.comparing(Transaction::getDate).reversed());
    }
    
    /**
     * 获取选中的文件
     * @return 选中的文件
     */
    public File getSelectedFile() {
        return selectedFile;
    }
    
    /**
     * 是否跳过标题行
     * @return 是否跳过标题行
     */
    public boolean isSkipHeader() {
        return skipHeaderCheckBox.isSelected();
    }
    
    /**
     * 是否合并数据
     * @return 是否合并数据
     */
    public boolean isMergeData() {
        return mergeDataCheckBox.isSelected();
    }
    
    /**
     * 获取预览数据
     * @return 预览数据列表
     */
    public List<Transaction> getPreviewData() {
        return previewData;
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
} 