package com.book.keeping.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * 自定义的加载指示器组件
 * 显示三个不同颜色的小圆点，循环动画表示加载状态
 */
public class LoadingIndicator extends Pane {
    
    private static final int DOT_COUNT = 3;
    private static final double DOT_RADIUS = 5;
    private static final double DOT_SPACING = 15;
    private static final Color[] DOT_COLORS = {
            Color.web("#1976d2"),
            Color.web("#4a6572"),
            Color.web("#64b5f6")
    };
    
    private final Circle[] dots = new Circle[DOT_COUNT];
    private final Timeline timeline;
    
    /**
     * 创建一个新的加载指示器
     */
    public LoadingIndicator() {
        // 创建圆点
        for (int i = 0; i < DOT_COUNT; i++) {
            dots[i] = createDot(i);
            getChildren().add(dots[i]);
        }
        
        // 设置动画
        timeline = createAnimation();
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        // 设置容器尺寸
        double width = DOT_COUNT * DOT_SPACING;
        double height = 2 * DOT_RADIUS;
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        
        // 使组件内部居中（重要）
        setLayoutX(-width / 2);
        setLayoutY(-height / 2);
        
        // 当组件可见时启动动画
        visibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                timeline.play();
            } else {
                timeline.pause();
            }
        });
    }
    
    /**
     * 创建一个圆点
     * @param index 圆点的索引
     * @return 创建的圆点
     */
    private Circle createDot(int index) {
        Circle dot = new Circle(DOT_RADIUS);
        dot.setFill(DOT_COLORS[index % DOT_COLORS.length]);
        dot.setCenterX(index * DOT_SPACING + DOT_RADIUS);
        dot.setCenterY(DOT_RADIUS);
        dot.setOpacity(0.3);
        return dot;
    }
    
    /**
     * 创建动画效果
     * @return 动画时间线
     */
    private Timeline createAnimation() {
        Timeline timeline = new Timeline();
        
        // 对每个圆点创建淡入淡出效果，错开时间形成波浪效果
        for (int i = 0; i < DOT_COUNT; i++) {
            KeyValue kv1 = new KeyValue(dots[i].opacityProperty(), 1.0);
            KeyValue kv2 = new KeyValue(dots[i].opacityProperty(), 0.3);
            KeyValue kv3 = new KeyValue(dots[i].scaleXProperty(), 1.2);
            KeyValue kv4 = new KeyValue(dots[i].scaleYProperty(), 1.2);
            KeyValue kv5 = new KeyValue(dots[i].scaleXProperty(), 1.0);
            KeyValue kv6 = new KeyValue(dots[i].scaleYProperty(), 1.0);
            
            // 设置动画关键帧，错开300毫秒
            double offsetMs = i * 300;
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(offsetMs), kv1, kv3),
                    new KeyFrame(Duration.millis(offsetMs + 500), kv2, kv4),
                    new KeyFrame(Duration.millis(offsetMs + 1000), kv5, kv6)
            );
        }
        
        return timeline;
    }
    
    /**
     * 启动动画
     */
    public void start() {
        timeline.play();
    }
    
    /**
     * 停止动画
     */
    public void stop() {
        timeline.pause();
    }
    

} 