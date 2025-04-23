package com.book.keeping.model;

import java.time.LocalDate;

/**
 * 交易数据模型类
 */
public class Transaction {
    private LocalDate date;
    private String description;
    private Double amount;
    private String category;
    private String source;
    private Boolean aiGenerated;

    // 默认构造函数
    public Transaction() {
        this.aiGenerated = false;
    }

    // 带参数的构造函数
    public Transaction(LocalDate date, String description, Double amount, String category, String source) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.source = source;
        this.aiGenerated = false;
    }

    // 带AI标记的构造函数
    public Transaction(LocalDate date, String description, Double amount, String category, String source, Boolean aiGenerated) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.source = source;
        this.aiGenerated = aiGenerated;
    }

    // Getter和Setter方法
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(Boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    // 用于UI显示的辅助getter方法
    public String getAiGeneratedDisplay() {
        return aiGenerated ? "Y" : "N";
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", source='" + source + '\'' +
                ", aiGenerated=" + aiGenerated +
                '}';
    }
} 