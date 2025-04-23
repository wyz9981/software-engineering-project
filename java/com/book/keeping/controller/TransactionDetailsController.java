package com.book.keeping.controller;

import com.book.keeping.model.Transaction;
import com.book.keeping.util.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

/**
 * 交易详情对话框控制器
 */
public class TransactionDetailsController {
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label descriptionLabel;
    
    @FXML
    private Label amountLabel;
    
    @FXML
    private Label categoryLabel;
    
    @FXML
    private Label sourceLabel;
    
    @FXML
    private Label aiGeneratedLabel;
    
    /**
     * 设置要显示的交易记录
     * @param transaction 交易记录
     */
    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        
        // 设置日期
        dateLabel.setText(transaction.getDate().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)));
        
        // 设置描述
        descriptionLabel.setText(transaction.getDescription());
        
        // 设置金额
        String amountText = String.format("%.2f", transaction.getAmount());
        amountLabel.setText(amountText);
        
        // 根据金额设置样式
        if (transaction.getAmount() > 0) {
            amountLabel.getStyleClass().add("positive-amount");
        } else if (transaction.getAmount() < 0) {
            amountLabel.getStyleClass().add("negative-amount");
        }
        
        // 设置分类
        categoryLabel.setText(transaction.getCategory());
        
        // 设置来源
        sourceLabel.setText(transaction.getSource());
        
        // 设置AI生成标记
        aiGeneratedLabel.setText(transaction.getAiGeneratedDisplay());
    }
} 