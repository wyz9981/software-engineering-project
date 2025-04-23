package com.book.keeping.util;

import com.book.keeping.model.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析工具类
 */
public class DataAnalysisUtil {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 按日期过滤交易记录
     * @param transactions 交易记录列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 过滤后的交易记录列表
     */
    public static List<Transaction> filterTransactionsByDate(List<Transaction> transactions,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    /**
     * 计算总收入
     * @param transactions 交易记录列表
     * @return 总收入
     */
    public static double calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    /**
     * 计算总支出
     * @param transactions 交易记录列表
     * @return 总支出
     */
    public static double calculateTotalExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    /**
     * 计算各类别支出
     * @param transactions 交易记录列表
     * @return 类别支出映射
     */
    public static Map<String, Double> calculateCategoryExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.summingDouble(Transaction::getAmount)
                ));
    }
    
    /**
     * 计算月度收支统计
     * @param transactions 交易记录列表
     * @return 月度统计映射
     */
    public static Map<String, Map<String, Double>> calculateMonthlyStats(List<Transaction> transactions) {
        Map<String, Map<String, Double>> monthlyStats = new TreeMap<>();
        
        // 按月份分组
        Map<String, List<Transaction>> monthlyTransactions = transactions.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getDate().format(MONTH_FORMATTER)
                ));
        
        // 计算每个月的收支
        monthlyTransactions.forEach((month, monthTransactions) -> {
            Map<String, Double> stats = new HashMap<>();
            stats.put("income", calculateTotalIncome(monthTransactions));
            stats.put("expense", calculateTotalExpense(monthTransactions));
            monthlyStats.put(month, stats);
        });
        
        return monthlyStats;
    }
    
    /**
     * 计算余额趋势
     * @param transactions 交易记录列表
     * @return 余额趋势映射
     */
    public static Map<String, Double> calculateBalanceTrend(List<Transaction> transactions) {
        // 按日期排序
        List<Transaction> sortedTransactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());
        
        // 计算每日余额
        Map<String, Double> balanceTrend = new TreeMap<>();
        double balance = 0;
        
        for (Transaction transaction : sortedTransactions) {
            balance += transaction.getAmount();
            balanceTrend.put(transaction.getDate().format(DATE_FORMATTER), balance);
        }
        
        return balanceTrend;
    }
} 