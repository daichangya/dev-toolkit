package com.daicy.devtools.plugin.impl;

import com.daicy.devtools.plugin.IconGenerator;
import com.daicy.devtools.plugin.JsonHighlighting;
import com.daicy.devtools.plugin.Plugin;
import com.daicy.devtools.plugin.impl.json.JsonAutoFixer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * JSON格式化插件
 */
public class JsonFormatterPlugin implements Plugin {
    private final ObjectMapper prettyMapper;
    private final ObjectMapper compactMapper;
    private CodeArea inputArea;
    private CodeArea outputArea;
    private ScrollPane outputScrollPane;
    
    private static final Pattern JSON_PATTERN = Pattern.compile(
            "\\{|\\}|\\[|\\]|,|\\b(true|false|null)\\b|\"([^\"]*)\"|:\s*(-?\\d+(\\.\\d+)?)"
    );

    private final JsonAutoFixer jsonAutoFixer;

    public JsonFormatterPlugin() {
        this.prettyMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.compactMapper = new ObjectMapper();
        this.jsonAutoFixer = new JsonAutoFixer();
    }

    @Override
    public String getName() {
        return "JSON格式化";
    }

    public String formatJson(String json) {
        try {
            // 使用JsonAutoFixer进行自动修复
            String fixedInput = jsonAutoFixer.fix(json);
            // 解析修复后的JSON并格式化
            Object jsonObj = prettyMapper.readValue(fixedInput, Object.class);
            String formatted = prettyMapper.writeValueAsString(jsonObj);
            // 如果进行了修复，在格式化结果前添加提示信息
            if (jsonAutoFixer.hasFixed()) {
                StringBuilder message = new StringBuilder("[自动修复] JSON格式已修复\n");
                for (String log : jsonAutoFixer.getFixLogs()) {
                    message.append("- ").append(log).append("\n");
                }
                message.append("\n").append(formatted);
                return message.toString();
            }
            return formatted;
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
                int lineNumber = e.getLocation().getLineNr();
                int columnNumber = e.getLocation().getColumnNr();
                String input = inputArea.getText();
                showError(e.getMessage(), input, lineNumber, columnNumber);
        } catch (Exception e) {
            String input = inputArea.getText();
            int errorPosition = findErrorPosition(e.getMessage(), input);
            showError(e.getMessage(), input, errorPosition);
        }
        return json;
    }

    @Override
    public String getDescription() {
        return "JSON字符串的格式化和压缩工具";
    }

    @Override
    public String getAuthor() {
        return "DevTools Team";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void initialize() {
        // 初始化已在构造函数中完成
    }

    @Override
    public Node getPluginNode() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // 创建输入区域
        inputArea = new CodeArea();
        VBox.setVgrow(inputArea, Priority.ALWAYS);

        // 创建输出区域
        outputArea = new CodeArea();
        outputArea.setEditable(false);
        outputArea.setParagraphGraphicFactory(LineNumberFactory.get(outputArea));
        outputScrollPane = new ScrollPane(outputArea);
        outputScrollPane.setFitToWidth(true);
        outputScrollPane.setFitToHeight(true);
        outputArea.getStylesheets().add(getClass().getResource("/styles/json-keywords.css").toExternalForm());
        VBox.setVgrow(outputScrollPane, Priority.ALWAYS);

        // 设置CodeArea的最小高度和首选高度
        outputArea.setMinHeight(200);
        outputArea.setPrefHeight(200);

        // 操作按钮区域
        HBox buttonBox = new HBox(10);
        Button formatBtn = new Button("格式化");
        Button compactBtn = new Button("压缩");
        Button clearBtn = new Button("清空");

        formatBtn.setOnAction(e -> formatJson());
        compactBtn.setOnAction(e -> compactJson());
        clearBtn.setOnAction(e -> clearAll());

        buttonBox.getChildren().addAll(formatBtn, compactBtn, clearBtn);

        container.getChildren().addAll(inputArea, buttonBox, outputScrollPane);
        return container;
    }

    private void formatJson() {
        String input = inputArea.getText();
        if (input == null || input.trim().isEmpty()) {
            outputArea.clear();
            outputArea.appendText("请输入要格式化的JSON字符串");
            return;
        }
        String formatted = formatJson(input);
        displayFormattedJson(formatted);
    }

    private void compactJson() {
        try {
            String input = inputArea.getText();
            if (input == null || input.trim().isEmpty()) {
                outputArea.clear();
                outputArea.appendText("请输入要压缩的JSON字符串");
                return;
            }

            // 先解析确保是有效的JSON，然后压缩
            Object json = prettyMapper.readValue(input, Object.class);
            String compacted = compactMapper.writeValueAsString(json);
            displayFormattedJson(compacted);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            int lineNumber = e.getLocation().getLineNr();
            int columnNumber = e.getLocation().getColumnNr();
            String input = inputArea.getText();
            showError(e.getMessage(), input, lineNumber, columnNumber);
        } catch (Exception e) {
            String input = inputArea.getText();
            int errorPosition = findErrorPosition(e.getMessage(), input);
            showError(e.getMessage(), input, errorPosition);
        }
    }

    private void showError(String errorMessage, String input, int lineNumber, int columnNumber) {
        outputArea.clear();
        String simplifiedError = simplifyErrorMessage(errorMessage);
        outputArea.appendText(simplifiedError + "\n行号: " + lineNumber + ", 列号: " + columnNumber + "\n\n");
        
        // 计算错误位置
        int errorPosition = 0;
        String[] lines = input.split("\n");
        for (int i = 0; i < lineNumber - 1 && i < lines.length; i++) {
            errorPosition += lines[i].length() + 1; // +1 for the newline character
        }
        errorPosition += columnNumber - 1;
        
        if (errorPosition >= 0 && errorPosition <= input.length()) {
            // 添加错误位置之前的文本
            if (errorPosition > 0) {
                outputArea.appendText(input.substring(0, errorPosition));
            }
            
            // 添加错误字符并设置样式
            int errorCharEnd = Math.min(errorPosition + 1, input.length());
            outputArea.appendText(input.substring(errorPosition, errorCharEnd));
            outputArea.setStyle(outputArea.getLength() - 1, outputArea.getLength(), Collections.singleton("red"));
            
            // 添加剩余文本
            if (errorCharEnd < input.length()) {
                outputArea.appendText(input.substring(errorCharEnd));
            }
        } else {
            outputArea.appendText(input);
        }
    }


    private String simplifyErrorMessage(String message) {
        // 简化常见的JSON错误消息
        if (message.contains("Unexpected character")) {
            return "格式错误：存在非法字符";
        } else if (message.contains("Unexpected end-of-input")) {
            return "格式错误：JSON不完整";
        } else if (message.contains("was expecting")) {
            return "格式错误：缺少必要的符号或结构";
        }
        return "格式错误：" + message;
    }

    private int findErrorPosition(String errorMessage, String input) {
        // 从错误消息中提取位置信息
        if (errorMessage.contains("line") && errorMessage.contains("column")) {
            try {
                // 提取行号和列号
                String[] parts = errorMessage.split("line")[1].split("column");
                int line = Integer.parseInt(parts[0].trim());
                int column = Integer.parseInt(parts[1].split("\\s")[0].trim());
                
                // 计算实际位置
                int position = 0;
                String[] lines = input.split("\n");
                for (int i = 0; i < line - 1 && i < lines.length; i++) {
                    position += lines[i].length() + 1; // +1 for the newline character
                }
                position += column - 1;
                return position;
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    private void showError(String errorMessage, String input, int errorPosition) {
        outputArea.clear();
        String simplifiedError = simplifyErrorMessage(errorMessage);
        outputArea.appendText(simplifiedError + "\n\n");
        
        if (errorPosition >= 0 && errorPosition <= input.length()) {
            // 添加错误位置之前的文本
            if (errorPosition > 0) {
                outputArea.appendText(input.substring(0, errorPosition));
            }
            
            // 添加错误字符并设置样式
            int errorCharEnd = Math.min(errorPosition + 1, input.length());
            outputArea.appendText(input.substring(errorPosition, errorCharEnd));
            outputArea.setStyle(outputArea.getLength() - 1, outputArea.getLength(), Collections.singleton("red"));
            
            // 添加剩余文本
            if (errorCharEnd < input.length()) {
                outputArea.appendText(input.substring(errorCharEnd));
            }
        } else {
            outputArea.appendText(input);
        }
    }

    private void displayFormattedJson(String json) {
        outputArea.clear();
        outputArea.appendText(json);
        // 设置样式高亮
        StyleSpans<Collection<String>> styleSpans = JsonHighlighting.highlight(json);
        outputArea.setStyleSpans(0, styleSpans);
    }

    private void clearAll() {
        inputArea.clear();
        outputArea.clear();
    }

    @Override
    public String getContent() {
        return inputArea.getText();
    }

    @Override
    public void setContent(String content) {
        inputArea.replaceText(0, inputArea.getLength(), content);
        formatJson(); // 自动格式化输入的内容
    }
    @Override
    public Image getIcon() {
        return IconGenerator.generateIcon(getName());
    }
}