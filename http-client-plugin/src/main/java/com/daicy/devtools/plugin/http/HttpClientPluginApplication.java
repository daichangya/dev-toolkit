package com.daicy.devtools.plugin.http;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HttpClientPluginApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局
        BorderPane root = new BorderPane();
        
        // 初始化HTTP客户端插件
        HttpClientPlugin httpClientPlugin = new HttpClientPlugin();
        httpClientPlugin.initialize();
        
        // 将插件添加到主布局
        root.setCenter(httpClientPlugin.getPluginNode());
        
        // 创建场景
        Scene scene = new Scene(root, 800, 600);
        
        // 设置窗口标题
        primaryStage.setTitle("HTTP Client Plugin Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 