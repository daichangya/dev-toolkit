package com.daicy.devtools.plugin.impl;

import com.daicy.devtools.plugin.IconGenerator;
import com.daicy.devtools.plugin.Plugin;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Base64转换插件
 */
public class Base64Plugin implements Plugin {
    private TextArea inputArea;
    private TextArea outputArea;


    @Override
    public void initialize() {

    }

    @Override
    public String getName() {
        return "Base64转换";
    }

    @Override
    public String getDescription() {
        return "文本内容的Base64编码和解码工具";
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public String getAuthor() {
        return "DevTools Team";
    }

    @Override
    public Node getPluginNode() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // 输入区域
        inputArea = new TextArea();
        inputArea.setPromptText("在此输入要转换的文本...");
        inputArea.setPrefRowCount(10);
        VBox.setVgrow(inputArea, Priority.ALWAYS);

        // 按钮区域
        HBox buttonBox = new HBox(10);
        Button encodeBtn = new Button("编码");
        Button decodeBtn = new Button("解码");
        Button clearBtn = new Button("清空");

        encodeBtn.setOnAction(e -> encode());
        decodeBtn.setOnAction(e -> decode());
        clearBtn.setOnAction(e -> clear());

        buttonBox.getChildren().addAll(encodeBtn, decodeBtn, clearBtn);

        // 输出区域
        outputArea = new TextArea();
        outputArea.setPromptText("转换结果将显示在这里...");
        outputArea.setPrefRowCount(10);
        outputArea.setEditable(false);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        container.getChildren().addAll(inputArea, buttonBox, outputArea);
        return container;
    }

    private void encode() {
        String input = inputArea.getText();
        if (input != null && !input.isEmpty()) {
            try {
                outputArea.setText(encodeBase64(input));
            } catch (Exception e) {
                outputArea.setText("编码失败：" + e.getMessage());
            }
        }
    }

    private void decode() {
        String input = inputArea.getText();
        if (input != null && !input.isEmpty()) {
            try {
                outputArea.setText(decodeBase64(input));
            } catch (Exception e) {
                outputArea.setText("解码失败：" + e.getMessage());
            }
        }
    }

    private void clear() {
        inputArea.clear();
        outputArea.clear();
    }

    public String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public String decodeBase64(String input) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 string", e);
        }
    }
    @Override
    public Image getIcon() {
        return IconGenerator.generateIcon(getName());
    }

    @Override
    public String getContent() {
        return inputArea.getText();
    }
    @Override
    public void setContent(String content) {
        inputArea.setText(content);
    }
}