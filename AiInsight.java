package com.book.keeping.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * AI洞察数据模型类
 * 存储AI生成的预算、储蓄和成本削减建议
 */
public class AiInsight {
    private LocalDate generatedDate;
    private double suggestedMonthlyBudget;
    private double suggestedSavingsGoal;
    private List<String> costReductionSuggestions;
    private String overview;
    
    // 默认构造函数
    public AiInsight() {
        this.generatedDate = LocalDate.now();
        this.costReductionSuggestions = new ArrayList<>();
    }
    
    // 带参数的构造函数
    public AiInsight(double suggestedMonthlyBudget, double suggestedSavingsGoal, 
                     List<String> costReductionSuggestions, String overview) {
        this.generatedDate = LocalDate.now();
        this.suggestedMonthlyBudget = suggestedMonthlyBudget;
        this.suggestedSavingsGoal = suggestedSavingsGoal;
        this.costReductionSuggestions = costReductionSuggestions;
        this.overview = overview;
    }
    
    // Getter和Setter方法
    public LocalDate getGeneratedDate() {
        return generatedDate;
    }
    
    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }
    
    public double getSuggestedMonthlyBudget() {
        return suggestedMonthlyBudget;
    }
    
    public void setSuggestedMonthlyBudget(double suggestedMonthlyBudget) {
        this.suggestedMonthlyBudget = suggestedMonthlyBudget;
    }
    
    public double getSuggestedSavingsGoal() {
        return suggestedSavingsGoal;
    }
    
    public void setSuggestedSavingsGoal(double suggestedSavingsGoal) {
        this.suggestedSavingsGoal = suggestedSavingsGoal;
    }
    
    public List<String> getCostReductionSuggestions() {
        return costReductionSuggestions;
    }
    
    public void setCostReductionSuggestions(List<String> costReductionSuggestions) {
        this.costReductionSuggestions = costReductionSuggestions;
    }
    
    public void addCostReductionSuggestion(String suggestion) {
        this.costReductionSuggestions.add(suggestion);
    }
    
    public String getOverview() {
        return overview;
    }
    
    public void setOverview(String overview) {
        this.overview = overview;
    }
    
    @Override
    public String toString() {
        return "AiInsight{" +
                "generatedDate=" + generatedDate +
                ", suggestedMonthlyBudget=" + suggestedMonthlyBudget +
                ", suggestedSavingsGoal=" + suggestedSavingsGoal +
                ", costReductionSuggestions=" + costReductionSuggestions +
                ", overview='" + overview + '\'' +
                '}';
    }
} 