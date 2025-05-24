package com.book.keeping.util;

import java.util.Arrays;
import java.util.List;

/**
 * 应用程序常量类
 */
public class Constants {
    // 应用名称
    public static final String APP_TITLE = "Personal Finance Manager";
    
    // 表格列名
    public static final String COL_DATE = "Date";
    public static final String COL_DESCRIPTION = "Description";
    public static final String COL_AMOUNT = "Amount";
    public static final String COL_CATEGORY = "Category";
    public static final String COL_SOURCE = "Source";
    public static final String COL_AI = "AI?";
    
    // 预定义的分类列表
    public static final List<String> CATEGORIES = Arrays.asList(
            "Salary",
            "Rent",
            "Groceries",
            "Utilities",
            "Transport",
            "Entertainment",
            "Dining Out",
            "Shopping",
            "Healthcare",
            "Education",
            "Savings",
            "Investment",
            "Insurance",
            "Other Income",
            "Other Expense",
            "Uncategorized"
    );
    
    // 预定义的资金来源列表
    public static final List<String> SOURCES = Arrays.asList(
            "Bank Transfer",
            "Credit Card",
            "Cash",
            "Alipay",
            "WeChat Pay",
            "Octopus Card",
            "PayPal",
            "Other"
    );
    
    // 日期格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    // 按钮文本
    public static final String BTN_ADD_TRANSACTION = "Add Transaction";
    public static final String BTN_CLEAR_FORM = "Clear Form";
    
    // 菜单项
    public static final String MENU_FILE = "File";
    public static final String MENU_EDIT = "Edit";
    public static final String MENU_VIEW = "View";
    public static final String MENU_HELP = "Help";
    
    // 菜单项 - 文件
    public static final String MENU_FILE_NEW = "New";
    public static final String MENU_FILE_OPEN = "Open";
    public static final String MENU_FILE_SAVE = "Save";
    public static final String MENU_FILE_EXIT = "Exit";
    
    // 菜单项 - 视图
    public static final String MENU_VIEW_DATA_ANALYSIS = "Data Analysis";
    public static final String MENU_VIEW_AI_INSIGHT = "AI Insight";
    
    // AI洞察相关
    public static final String AI_INSIGHT_TITLE = "AI 智能财务洞察";
    public static final String AI_INSIGHT_REFRESH = "刷新洞察";
    public static final String AI_INSIGHT_LOADING = "正在生成AI洞察...";
    public static final String AI_INSIGHT_LAST_UPDATED = "最后更新: ";
    
    // AI洞察面板标题
    public static final String AI_INSIGHT_OVERVIEW_PANEL = "财务状况概览";
    public static final String AI_INSIGHT_BUDGET_PANEL = "预算建议";
    public static final String AI_INSIGHT_SUGGESTIONS_PANEL = "成本削减建议";
    public static final String AI_INSIGHT_CATEGORY_PANEL = "支出分类分析";
    
    // AI洞察标签
    public static final String AI_INSIGHT_MONTHLY_BUDGET = "建议月度预算:";
    public static final String AI_INSIGHT_SAVINGS_GOAL = "建议储蓄目标:";
    
    // AI数据处理相关
    public static final int AI_DATA_DEFAULT_MONTHS = 6;  // 默认分析最近6个月的数据
    public static final int AI_TOP_EXPENSES_LIMIT = 3;   // 默认显示前3个主要支出类别
} 