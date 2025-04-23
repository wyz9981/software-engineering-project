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
} 