package com.book.keeping.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * API配置类
 * 负责管理和提供API相关的配置信息
 */
public class ApiConfig {
    
    // DeepSeek API 配置
    private static final String DEFAULT_DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEFAULT_DEEPSEEK_MODEL = "deepseek-chat";
    
    // 从配置文件或环境变量中读取的实际配置
    private static String deepseekApiUrl;
    private static String deepseekApiKey;
    private static String deepseekModel;
    
    // 初始化标志
    private static boolean initialized = false;
    
    /**
     * 初始化API配置
     * 尝试从配置文件和环境变量读取配置
     */
    public static void init() {
        if (initialized) {
            return;
        }
        
        // 尝试从配置文件读取
        Properties properties = new Properties();
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                
                // 读取配置项
                deepseekApiUrl = properties.getProperty("deepseek.api.url", DEFAULT_DEEPSEEK_API_URL);
                deepseekApiKey = properties.getProperty("deepseek.api.key", "");
                deepseekModel = properties.getProperty("deepseek.model", DEFAULT_DEEPSEEK_MODEL);
            }
        } catch (IOException e) {
            System.err.println("未能读取配置文件: " + e.getMessage());
        }
        
        // 如果配置文件中没有值，尝试从环境变量读取
        if (deepseekApiUrl == null || deepseekApiUrl.isEmpty()) {
            deepseekApiUrl = System.getenv("DEEPSEEK_API_URL");
            if (deepseekApiUrl == null || deepseekApiUrl.isEmpty()) {
                deepseekApiUrl = DEFAULT_DEEPSEEK_API_URL;
            }
        }
        
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            deepseekApiKey = System.getenv("DEEPSEEK_API_KEY");
            if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
                // 使用应用程序默认值，实际应用中应提示用户配置
                deepseekApiKey = "YOUR_DEEPSEEK_API_KEY";
            }
        }
        
        if (deepseekModel == null || deepseekModel.isEmpty()) {
            deepseekModel = System.getenv("DEEPSEEK_MODEL");
            if (deepseekModel == null || deepseekModel.isEmpty()) {
                deepseekModel = DEFAULT_DEEPSEEK_MODEL;
            }
        }
        
        initialized = true;
    }
    
    /**
     * 确保配置已初始化
     */
    private static void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }
    
    /**
     * 获取DeepSeek API URL
     * @return API URL
     */
    public static String getDeepseekApiUrl() {
        ensureInitialized();
        return deepseekApiUrl;
    }
    
    /**
     * 获取DeepSeek API Key
     * @return API Key
     */
    public static String getDeepseekApiKey() {
        ensureInitialized();
        return deepseekApiKey;
    }
    
    /**
     * 获取DeepSeek 模型名称
     * @return 模型名称
     */
    public static String getDeepseekModel() {
        ensureInitialized();
        return deepseekModel;
    }
    
    /**
     * 设置DeepSeek API Key
     * @param apiKey API Key
     */
    public static void setDeepseekApiKey(String apiKey) {
        ensureInitialized();
        deepseekApiKey = apiKey;
    }
} 