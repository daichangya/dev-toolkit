package com.daicy.devtools.plugin.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一的异常处理工具类
 */
public class ExceptionHandler {
    private static final Logger logger = Logger.getLogger(ExceptionHandler.class.getName());

    /**
     * 处理配置相关的异常
     * @param e 异常对象
     * @param message 错误信息
     */
    public static void handleConfigException(IOException e, String message) {
        logger.log(Level.SEVERE, message + ": " + e.getMessage(), e);
        handleException(e, "配置错误", message);
    }

    /**
     * 处理异常并显示错误对话框
     *
     * @param throwable 异常对象
     * @param title     对话框标题
     * @param header    对话框头信息
     */
    public static void handleException(Throwable throwable, String title, String header) {
        // 记录错误日志
        logger.log(Level.SEVERE, throwable.getMessage(), throwable);

        // 确保在JavaFX应用程序线程中运行
        Platform.runLater(() -> showErrorDialog(throwable, title, header));
    }

    /**
     * 处理异常并显示错误对话框（使用默认标题）
     *
     * @param throwable 异常对象
     */
    public static void handleException(Throwable throwable) {
        handleException(throwable, "错误", "发生错误");
    }

    /**
     * 显示错误对话框
     *
     * @param throwable 异常对象
     * @param title     对话框标题
     * @param header    对话框头信息
     */
    private static void showErrorDialog(Throwable throwable, String title, String header) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(throwable.getMessage());

        // 获取异常堆栈信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        // 创建可展开的异常堆栈详情区域
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
}