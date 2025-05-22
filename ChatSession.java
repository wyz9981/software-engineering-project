package com.book.keeping.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话模型类
 * 管理聊天历史和消息列表
 */
public class ChatSession {
    private String sessionId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private ObservableList<ChatMessage> messages;
    
    /**
     * 默认构造函数
     */
    public ChatSession() {
        this.sessionId = generateSessionId();
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.messages = FXCollections.observableArrayList();
    }
    
    /**
     * 添加系统提示消息，用于设置AI的行为和知识
     * @param systemPrompt 系统提示内容
     */
    public void initWithSystemPrompt(String systemPrompt) {
        // 这里我们不在UI上显示系统提示
        // 但我们在构建给DeepSeek的提示时会使用它
    }
    
    /**
     * 添加用户消息
     * @param content 消息内容
     * @return 添加的消息
     */
    public ChatMessage addUserMessage(String content) {
        return addMessage(ChatMessage.createUserMessage(content));
    }
    
    /**
     * 添加AI消息
     * @param content 消息内容
     * @return 添加的消息
     */
    public ChatMessage addAiMessage(String content) {
        return addMessage(ChatMessage.createAiMessage(content));
    }
    
    /**
     * 添加消息
     * @param message 要添加的消息
     * @return 添加的消息
     */
    public ChatMessage addMessage(ChatMessage message) {
        messages.add(message);
        lastUpdated = LocalDateTime.now();
        return message;
    }
    
    /**
     * 获取所有消息
     * @return 消息列表
     */
    public ObservableList<ChatMessage> getMessages() {
        return messages;
    }
    
    /**
     * 获取对话历史
     * 用于构建API请求
     * @return 消息列表，格式适用于DeepSeek API
     */
    public List<Object> getConversationHistory() {
        List<Object> history = new ArrayList<>();
        
        // 添加消息
        for (ChatMessage message : messages) {
            Object messageObj = new Object() {
                public final String role = message.isUserMessage() ? "user" : "assistant";
                public final String content = message.getContent();
            };
            history.add(messageObj);
        }
        
        return history;
    }
    
    /**
     * 清空聊天历史
     */
    public void clear() {
        messages.clear();
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 清空聊天消息历史
     * 该操作将删除所有已保存的消息
     */
    public void clearMessages() {
        messages.clear();
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 生成会话ID
     * @return 会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    // Getter和Setter方法
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public void setMessages(ObservableList<ChatMessage> messages) {
        this.messages = messages;
    }
    
    @Override
    public String toString() {
        return "ChatSession{" +
                "sessionId='" + sessionId + '\'' +
                ", createdAt=" + createdAt +
                ", lastUpdated=" + lastUpdated +
                ", messageCount=" + messages.size() +
                '}';
    }
} 