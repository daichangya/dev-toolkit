package com.daicy.devtools.plugin.impl;

import com.daicy.devtools.plugin.Plugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class JsonToBeanPlugin implements Plugin {
    private TextArea jsonInput;
    private TextArea beanOutput;
    private TextField packageNameField;
    private TextField classNameField;
    private final ObjectMapper objectMapper;
    
    public JsonToBeanPlugin() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @Override
    public void initialize() {
        // 初始化时不需要特殊处理
    }
    
    @Override
    public String getName() {
        return "JSON转JavaBean";
    }
    
    @Override
    public String getDescription() {
        return "将JSON字符串转换为Java Bean类";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getAuthor() {
        return "DevTools Team";
    }
    
    @Override
    public Node getPluginNode() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // 创建配置面板
        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(0, 0, 10, 0));
        
        // 包名输入框
        HBox packageBox = new HBox(10);
        packageBox.setAlignment(Pos.CENTER_LEFT);
        Label packageLabel = new Label("包名:");
        packageNameField = new TextField("com.example");
        packageBox.getChildren().addAll(packageLabel, packageNameField);
        
        // 类名输入框
        HBox classBox = new HBox(10);
        classBox.setAlignment(Pos.CENTER_LEFT);
        Label classLabel = new Label("类名:");
        classNameField = new TextField("MyBean");
        classBox.getChildren().addAll(classLabel, classNameField);
        
        configBox.getChildren().addAll(packageBox, classBox);
        
        // 创建主要内容区域
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5); // 设置分割位置为中间
        
        // 左侧JSON输入区域
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.setMinWidth(300); // 设置最小宽度
        leftPane.setMaxWidth(800); // 设置最大宽度
        Label jsonLabel = new Label("JSON输入:");
        jsonInput = new TextArea();
        jsonInput.setPromptText("在此输入JSON字符串");
        jsonInput.setMinHeight(300);
        jsonInput.setPrefHeight(400);
        jsonInput.setWrapText(true); // 启用自动换行
        VBox.setVgrow(jsonInput, Priority.ALWAYS);
        Button formatJsonBtn = new Button("格式化JSON");
        formatJsonBtn.setOnAction(e -> formatJson());
        leftPane.getChildren().addAll(jsonLabel, jsonInput, formatJsonBtn);
        
        // 右侧Java类输出区域
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        rightPane.setMinWidth(300); // 设置最小宽度
        rightPane.setMaxWidth(800); // 设置最大宽度
        Label beanLabel = new Label("Java类输出:");
        beanOutput = new TextArea();
        beanOutput.setEditable(false);
        beanOutput.setMinHeight(300);
        beanOutput.setPrefHeight(400);
        beanOutput.setWrapText(true); // 启用自动换行
        VBox.setVgrow(beanOutput, Priority.ALWAYS);
        HBox buttonBox = new HBox(10);
        Button generateBtn = new Button("生成Java类");
        generateBtn.setOnAction(e -> generateJavaBean());
        Button copyBtn = new Button("复制代码");
        copyBtn.setOnAction(e -> copyToClipboard());
        buttonBox.getChildren().addAll(generateBtn, copyBtn);
        rightPane.getChildren().addAll(beanLabel, beanOutput, buttonBox);
        
        splitPane.getItems().addAll(leftPane, rightPane);
        
        root.setTop(configBox);
        root.setCenter(splitPane);
        
        return root;
    }
    
    private void formatJson() {
        try {
            String json = jsonInput.getText();
            if (json == null || json.trim().isEmpty()) {
                showError("错误", "请输入JSON字符串");
                return;
            }
            
            // 使用Jackson格式化JSON
            JsonNode jsonNode = objectMapper.readTree(json);
            String formattedJson = objectMapper.writeValueAsString(jsonNode);
            jsonInput.setText(formattedJson);
            
        } catch (Exception e) {
            showError("错误", "JSON格式化失败: " + e.getMessage());
        }
    }
    
    private void generateJavaBean() {
        try {
            String json = jsonInput.getText();
            if (json == null || json.trim().isEmpty()) {
                showError("错误", "请输入JSON字符串");
                return;
            }
            
            String packageName = packageNameField.getText();
            String className = classNameField.getText();
            
            if (className == null || className.trim().isEmpty()) {
                showError("错误", "请输入类名");
                return;
            }
            
            // 解析JSON并生成Java类代码
            JsonNode rootNode = objectMapper.readTree(json);
            StringBuilder code = new StringBuilder();
            
            // 生成包声明
            if (packageName != null && !packageName.trim().isEmpty()) {
                code.append("package ").append(packageName).append(";\n\n");
            }
            
            // 添加导入语句
            code.append("import java.util.List;\n");
            code.append("import java.util.Map;\n\n");
            
            // 生成类定义
            code.append("public class ").append(className).append(" {\n");
            
            // 生成字段
            generateFields(rootNode, code, 1);
            
            // 生成getter和setter方法
            generateGettersAndSetters(rootNode, code, className);
            
            code.append("}\n");
            
            beanOutput.setText(code.toString());
            
        } catch (Exception e) {
            showError("错误", "生成Java类失败: " + e.getMessage());
        }
    }
    
    private void generateFields(JsonNode node, StringBuilder code, int indentLevel) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        String indent = "    ".repeat(indentLevel);
        
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            
            // 生成字段
            code.append(indent).append("private ");
            
            if (fieldValue.isObject()) {
                code.append(toClassName(fieldName));
            } else if (fieldValue.isArray()) {
                code.append("List<");
                if (fieldValue.size() > 0 && fieldValue.get(0).isObject()) {
                    code.append(toClassName(fieldName));
                } else {
                    code.append(getJavaType(fieldValue.get(0)));
                }
                code.append(">");
            } else {
                code.append(getJavaType(fieldValue));
            }
            
            code.append(" ").append(toCamelCase(fieldName)).append(";\n");
        }
        code.append("\n");
    }
    
    private void generateGettersAndSetters(JsonNode node, StringBuilder code, String className) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            String camelCaseFieldName = toCamelCase(fieldName);
            String capitalizedFieldName = capitalize(camelCaseFieldName);
            
            // Getter
            code.append("    public ");
            if (fieldValue.isObject()) {
                code.append(toClassName(fieldName));
            } else if (fieldValue.isArray()) {
                code.append("List<");
                if (fieldValue.size() > 0 && fieldValue.get(0).isObject()) {
                    code.append(toClassName(fieldName));
                } else {
                    code.append(getJavaType(fieldValue.get(0)));
                }
                code.append(">");
            } else {
                code.append(getJavaType(fieldValue));
            }
            code.append(" get").append(capitalizedFieldName).append("() {\n");
            code.append("        return ").append(camelCaseFieldName).append(";\n");
            code.append("    }\n\n");
            
            // Setter
            code.append("    public void set").append(capitalizedFieldName).append("(");
            if (fieldValue.isObject()) {
                code.append(toClassName(fieldName));
            } else if (fieldValue.isArray()) {
                code.append("List<");
                if (fieldValue.size() > 0 && fieldValue.get(0).isObject()) {
                    code.append(toClassName(fieldName));
                } else {
                    code.append(getJavaType(fieldValue.get(0)));
                }
                code.append(">");
            } else {
                code.append(getJavaType(fieldValue));
            }
            code.append(" ").append(camelCaseFieldName).append(") {\n");
            code.append("        this.").append(camelCaseFieldName).append(" = ").append(camelCaseFieldName).append(";\n");
            code.append("    }\n\n");
        }
    }
    
    private String getJavaType(JsonNode node) {
        if (node == null || node.isNull()) return "Object";
        if (node.isTextual()) return "String";
        if (node.isInt()) return "Integer";
        if (node.isLong()) return "Long";
        if (node.isDouble() || node.isFloat()) return "Double";
        if (node.isBoolean()) return "Boolean";
        if (node.isObject()) return "Object";
        if (node.isArray()) return "List<Object>";
        return "Object";
    }
    
    private String toClassName(String name) {
        return capitalize(toCamelCase(name));
    }
    
    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            if (c == '_' || c == '-') {
                nextUpper = true;
            } else {
                if (i == 0) {
                    result.append(Character.toLowerCase(c));
                } else if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        
        return result.toString();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    private void copyToClipboard() {
        String content = beanOutput.getText();
        if (content != null && !content.trim().isEmpty()) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            
            showInfo("成功", "代码已复制到剪贴板");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @Override
    public void setContent(String content) {
        if (content != null) {
            jsonInput.setText(content);
        }
    }
    
    @Override
    public String getContent() {
        return jsonInput.getText();
    }
}