package com.book.keeping.model;

import java.time.LocalDateTime;

/**
 * 聊天消息模型类
 * 存储聊天消息的信息
 */
public class ChatMessage {
    public enum Sender {
        USER,
        AI
    }
    
    private Sender sender;
    private String content;
    private LocalDateTime timestamp;
    
    /**
     * 默认构造函数
     */
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 带参数的构造函数
     * @param sender 消息发送者
     * @param content 消息内容
     */
    public ChatMessage(Sender sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 带参数的构造函数
     * @param sender 消息发送者
     * @param content 消息内容
     * @param timestamp 时间戳
     */
    public ChatMessage(Sender sender, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
    
    /**
     * 创建用户消息
     * @param content 消息内容
     * @return ChatMessage 用户消息
     */
    public static ChatMessage createUserMessage(String content) {
        return new ChatMessage(Sender.USER, content);
    }
    
    /**
     * 创建AI消息
     * @param content 消息内容
     * @return ChatMessage AI消息
     */
    public static ChatMessage createAiMessage(String content) {
        return new ChatMessage(Sender.AI, content);
    }
    
    // Getter 和 Setter 方法
    public Sender getSender() {
        return sender;
    }
    
    public void setSender(Sender sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 判断是否是用户消息
     * @return 是否是用户消息
     */
    public boolean isUserMessage() {
        return this.sender == Sender.USER;
    }
    
    /**
     * 判断是否是AI消息
     * @return 是否是AI消息
     */
    public boolean isAiMessage() {
        return this.sender == Sender.AI;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "sender=" + sender +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 