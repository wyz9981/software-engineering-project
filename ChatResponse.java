package com.book.keeping.model;

/**
 * 聊天响应数据封装类
 * 用于从服务层返回聊天数据，避免在非JavaFX线程中修改ObservableList
 */
public class ChatResponse {
    private String userMessage;
    private String aiResponse;
    private boolean isError;
    
    /**
     * 创建成功的聊天响应
     * @param userMessage 用户消息
     * @param aiResponse AI响应
     */
    public ChatResponse(String userMessage, String aiResponse) {
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.isError = false;
    }
    
    /**
     * 创建错误的聊天响应
     * @param userMessage 用户消息
     * @param errorMessage 错误消息
     * @param isError 是否是错误
     */
    public ChatResponse(String userMessage, String errorMessage, boolean isError) {
        this.userMessage = userMessage;
        this.aiResponse = errorMessage;
        this.isError = isError;
    }
    
    /**
     * 获取用户消息
     * @return 用户消息
     */
    public String getUserMessage() {
        return userMessage;
    }
    
    /**
     * 获取AI响应
     * @return AI响应
     */
    public String getAiResponse() {
        return aiResponse;
    }
    
    /**
     * 是否是错误响应
     * @return 是否是错误
     */
    public boolean isError() {
        return isError;
    }
} 