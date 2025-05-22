package com.book.keeping;

import com.book.keeping.util.ApiConfig;
import com.book.keeping.util.Constants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 个人理财管理工具主应用类
 */
public class FinanceApplication extends Application {
    
    // 保存主场景的引用以便更新主题
    private static Scene mainScene;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 初始化API配置
            ApiConfig.init();
            
            // 加载FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Parent root = loader.load();
            
            // 创建场景
            mainScene = new Scene(root, 1000, 650);
            
            // 设置窗口属性
            primaryStage.setTitle("Personal Finance Manager");
            primaryStage.setWidth(1400);
            primaryStage.setHeight(900);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.setScene(mainScene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取主场景
     * @return 主场景
     */
    public static Scene getMainScene() {
        return mainScene;
    }
    
    /**
     * 应用入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
} 