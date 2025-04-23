package com.book.keeping.util;

import com.book.keeping.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV文件导入工具类
 */
public class CSVImporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    
    /**
     * 从CSV文件导入交易记录
     * @param filePath CSV文件路径
     * @param skipHeader 是否跳过标题行
     * @return 导入结果，包含成功导入的交易记录和错误信息
     */
    public static ImportResult importFromCSV(String filePath, boolean skipHeader) {
        List<Transaction> transactions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || (skipHeader && lineNumber == 1)) { // 根据参数决定是否跳过标题行
                    continue;
                }
                
                try {
                    Transaction transaction = parseTransaction(line);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            errors.add("Error reading file: " + e.getMessage());
        }
        
        return new ImportResult(transactions, errors);
    }
    
    /**
     * 解析单行CSV数据为交易记录
     * @param line CSV行数据
     * @return 交易记录对象
     * @throws Exception 解析错误时抛出异常
     */
    private static Transaction parseTransaction(String line) throws Exception {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            throw new Exception("Invalid format: expected at least 5 columns");
        }
        
        try {
            // 解析日期
            LocalDate date = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
            
            // 解析描述
            String description = parts[1].trim();
            if (description.isEmpty()) {
                throw new Exception("Description cannot be empty");
            }
            
            // 解析金额
            double amount;
            try {
                amount = Double.parseDouble(parts[2].trim());
            } catch (NumberFormatException e) {
                throw new Exception("Invalid amount format: " + parts[2]);
            }
            
            // 解析分类
            String category = parts[3].trim();
            if (category.isEmpty()) {
                category = "Uncategorized";
            }
            
            // 解析来源
            String source = parts[4].trim();
            if (source.isEmpty()) {
                source = "Other";
            }
            
            // 解析AI标记（可选）
            boolean aiGenerated = false;
            if (parts.length > 5) {
                aiGenerated = Boolean.parseBoolean(parts[5].trim());
            }
            
            return new Transaction(date, description, amount, category, source, aiGenerated);
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format: " + parts[0]);
        }
    }
    
    /**
     * 导入结果类
     */
    public static class ImportResult {
        private final List<Transaction> transactions;
        private final List<String> errors;
        
        public ImportResult(List<Transaction> transactions, List<String> errors) {
            this.transactions = transactions;
            this.errors = errors;
        }
        
        public List<Transaction> getTransactions() {
            return transactions;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public int getSuccessCount() {
            return transactions.size();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
    }
} 