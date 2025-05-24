package com.book.keeping.ui;

import com.book.keeping.model.ChatMessage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.format.DateTimeFormatter;

/**
 * 自定义的聊天消息单元格
 * 用于在ListView中显示ChatMessage对象，并根据消息类型（用户/AI）应用不同的样式
 */
public class ChatMessageCell extends ListCell<ChatMessage> {
    
    private final HBox hbox = new HBox();
    private final VBox messageBox = new VBox();
    private final Label nameLabel = new Label();
    private final TextFlow messageFlow = new TextFlow();
    private final Label timeLabel = new Label();
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public ChatMessageCell() {
        super();
        
        // 设置消息容器样式
        messageBox.setSpacing(3);
        messageBox.setPadding(new Insets(8));
        messageBox.setMaxWidth(550); // 限制消息最大宽度
        
        // 设置名称标签样式
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        // 设置时间标签样式
        timeLabel.setFont(Font.font("System", 10));
        timeLabel.setTextFill(Color.GRAY);
        
        // 将组件添加到消息容器
        messageBox.getChildren().addAll(nameLabel, messageFlow, timeLabel);
        
        // 将消息容器添加到HBox
        hbox.getChildren().add(messageBox);
        hbox.setPadding(new Insets(5, 10, 5, 10));
        hbox.setSpacing(10);
    }
    
    @Override
    protected void updateItem(ChatMessage message, boolean empty) {
        super.updateItem(message, empty);
        
        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        
        // 清除之前的文本内容
        messageFlow.getChildren().clear();
        
        // 根据消息类型设置不同的样式
        if (message.isUserMessage()) {
            // 用户消息样式 - 蓝色靠右
            messageBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10 0 10 10;");
            nameLabel.setText("ME");
            nameLabel.setTextFill(Color.web("#1976d2"));
            hbox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // AI消息样式 - 灰色靠左
            messageBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 0 10 10 10;");
            nameLabel.setText("AI Financial Assistant");
            nameLabel.setTextFill(Color.web("#4a6572"));
            hbox.setAlignment(Pos.CENTER_LEFT);
        }
        
        // 设置消息内容
        Text messageText = new Text(message.getContent());
        messageText.setWrappingWidth(530); // 设置文本自动换行宽度
        messageFlow.getChildren().add(messageText);
        
        // 设置时间标签
        timeLabel.setText(message.getTimestamp().format(TIME_FORMATTER));
        
        // 设置单元格的图形组件
        setGraphic(hbox);
    }
} 