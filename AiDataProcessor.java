package com.book.keeping.util;

import com.book.keeping.model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI数据处理工具类
 * 负责处理和格式化交易数据，以便AI分析
 */
public class AiDataProcessor {
    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取最近N个月的交易数据
     * @param transactions 所有交易数据
     * @param months 月数
     * @return 过滤后的交易数据
     */
    public static List<Transaction> getRecentTransactions(List<Transaction> transactions, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 计算月度收入
     * @param transactions 交易数据
     * @return 月度收入映射 (年月 -> 金额)
     */
    public static Map<YearMonth, Double> calculateMonthlyIncome(List<Transaction> transactions) {
        Map<YearMonth, Double> monthlyIncome = new TreeMap<>();
        
        transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .forEach(t -> {
                    YearMonth month = YearMonth.from(t.getDate());
                    monthlyIncome.merge(month, t.getAmount(), Double::sum);
                });
        
        return monthlyIncome;
    }
    
    /**
     * 计算月度支出
     * @param transactions 交易数据
     * @return 月度支出映射 (年月 -> 金额)
     */
    public static Map<YearMonth, Double> calculateMonthlyExpense(List<Transaction> transactions) {
        Map<YearMonth, Double> monthlyExpense = new TreeMap<>();
        
        transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .forEach(t -> {
                    YearMonth month = YearMonth.from(t.getDate());
                    monthlyExpense.merge(month, Math.abs(t.getAmount()), Double::sum);
                });
        
        return monthlyExpense;
    }
    
    /**
     * 计算类别支出
     * @param transactions 交易数据
     * @return 类别支出映射 (类别 -> 金额)
     */
    public static Map<String, Double> calculateCategoryExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));
    }
    
    /**
     * 计算每月平均收入
     * @param transactions 交易数据
     * @return 月平均收入
     */
    public static double calculateAverageMonthlyIncome(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return 0.0;
        }
        
        // 计算交易跨越的月数
        LocalDate minDate = transactions.stream()
                .min(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate)
                .orElse(LocalDate.now());
        
        LocalDate maxDate = transactions.stream()
                .max(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate)
                .orElse(LocalDate.now());
        
        // 计算月数（至少为1）
        long months = YearMonth.from(maxDate).getMonthValue() - YearMonth.from(minDate).getMonthValue() + 1;
        months = Math.max(1, months);
        
        // 计算总收入
        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        return totalIncome / months;
    }
    
    /**
     * 计算每月平均支出
     * @param transactions 交易数据
     * @return 月平均支出
     */
    public static double calculateAverageMonthlyExpense(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return 0.0;
        }
        
        // 计算交易跨越的月数
        LocalDate minDate = transactions.stream()
                .min(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate)
                .orElse(LocalDate.now());
        
        LocalDate maxDate = transactions.stream()
                .max(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate)
                .orElse(LocalDate.now());
        
        // 计算月数（至少为1）
        long months = YearMonth.from(maxDate).getMonthValue() - YearMonth.from(minDate).getMonthValue() + 1;
        months = Math.max(1, months);
        
        // 计算总支出
        double totalExpense = transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(t -> Math.abs(t.getAmount()))
                .sum();
        
        return totalExpense / months;
    }
    
    /**
     * 找出支出最多的类别
     * @param transactions 交易数据
     * @param limit 返回数量
     * @return 支出最多的类别列表
     */
    public static List<Map.Entry<String, Double>> getTopExpenseCategories(
            List<Transaction> transactions, int limit) {
        
        Map<String, Double> categoryExpenses = calculateCategoryExpenses(transactions);
        
        return categoryExpenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算支出增长趋势
     * @param transactions 交易数据
     * @param months 月数
     * @return 支出增长率
     */
    public static double calculateExpenseGrowthRate(List<Transaction> transactions, int months) {
        if (transactions.isEmpty() || months < 2) {
            return 0.0;
        }
        
        // 按月分组交易
        Map<YearMonth, Double> monthlyExpenses = calculateMonthlyExpense(transactions);
        
        if (monthlyExpenses.size() < 2) {
            return 0.0;
        }
        
        // 计算前半段和后半段的平均支出
        List<Double> expenseValues = new ArrayList<>(monthlyExpenses.values());
        int midPoint = expenseValues.size() / 2;
        
        double firstHalfAvg = expenseValues.subList(0, midPoint).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double secondHalfAvg = expenseValues.subList(midPoint, expenseValues.size()).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // 计算增长率
        if (firstHalfAvg == 0.0) {
            return 0.0;
        }
        
        return (secondHalfAvg - firstHalfAvg) / firstHalfAvg;
    }
} 