package com.daicy.devtools.plugin.impl;

import com.daicy.devtools.plugin.IconGenerator;
import com.daicy.devtools.plugin.Plugin;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class HostEditorPlugin implements Plugin {
    private static final String HOSTS_FILE = System.getProperty("os.name").toLowerCase().contains("windows") ?
            "C:\\Windows\\System32\\drivers\\etc\\hosts" : "/etc/hosts";


    @Override
    public String getAuthor() {
        return "DevTools Team";
    }
    private final BorderPane rootPane;
    private final CodeArea codeArea;
    private File currentFile;

    public HostEditorPlugin() {
        rootPane = new BorderPane();
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setWrapText(true);
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        
        // 创建工具栏
        ToolBar toolbar = new ToolBar();
        Button loadHostsButton = new Button("加载Hosts文件");
        Button saveHostsButton = new Button("保存Hosts文件");
        Button commentButton = new Button("注释选中行");
        Button uncommentButton = new Button("取消注释");
        
        toolbar.getItems().addAll(loadHostsButton, saveHostsButton, commentButton, uncommentButton);
        
        // 设置按钮事件
        loadHostsButton.setOnAction(e -> loadHostsFile());
        saveHostsButton.setOnAction(e -> saveHostsFile());
        commentButton.setOnAction(e -> commentSelectedLines());
        uncommentButton.setOnAction(e -> uncommentSelectedLines());
        
        VBox contentBox = new VBox(toolbar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        rootPane.setCenter(contentBox);
        
        // 加载hosts文件
        Platform.runLater(this::loadHostsFile);
    }

    private void loadHostsFile() {
        try {
            String content = Files.readString(Path.of(HOSTS_FILE));
            codeArea.replaceText(content);
            currentFile = new File(HOSTS_FILE);
        } catch (Exception e) {
            showError("错误", "无法读取hosts文件：" + e.getMessage());
        }
    }

    private void saveHostsFile() {
        try {
            // 创建备份
            Path hostsPath = Path.of(HOSTS_FILE);
            Path backupPath = Path.of(HOSTS_FILE + ".bak");
            Files.copy(hostsPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 保存新内容
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认保存");
            alert.setHeaderText(null);
            alert.setContentText("确定要保存修改到系统hosts文件吗？");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Files.writeString(hostsPath, codeArea.getText());
            }
        } catch (Exception e) {
            showError("错误", "无法保存hosts文件：" + e.getMessage());
        }
    }

    private String processLines(String text, boolean comment) {
        String[] lines = text.split("\n");
        StringBuilder newText = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (comment) {
                if (!trimmedLine.startsWith("#")) {
                    newText.append("# ").append(line);
                } else {
                    newText.append(line);
                }
            } else {
                if (trimmedLine.startsWith("#")) {
                    newText.append(trimmedLine.substring(trimmedLine.indexOf('#') + 1).trim());
                } else {
                    newText.append(line);
                }
            }
            newText.append("\n");
        }
        return newText.toString().trim();
    }

    private void commentSelectedLines() {
        String selectedText = codeArea.getSelectedText();
        if (selectedText.isEmpty()) {
            // 处理光标所在行
            int caretPosition = codeArea.getCaretPosition();
            int lineNumber = codeArea.getCurrentParagraph();
            String currentLine = codeArea.getParagraph(lineNumber).getText();
            String processedLine = processLines(currentLine, true);
            codeArea.replaceText(lineNumber, 0, lineNumber, currentLine.length(), processedLine);
            codeArea.moveTo(caretPosition);
        } else {
            // 处理选中的文本
            String processedText = processLines(selectedText, true);
            codeArea.replaceSelection(processedText);
        }
    }

    private void uncommentSelectedLines() {
        String selectedText = codeArea.getSelectedText();
        if (selectedText.isEmpty()) {
            // 处理光标所在行
            int caretPosition = codeArea.getCaretPosition();
            int lineNumber = codeArea.getCurrentParagraph();
            String currentLine = codeArea.getParagraph(lineNumber).getText();
            String processedLine = processLines(currentLine, false);
            codeArea.replaceText(lineNumber, 0, lineNumber, currentLine.length(), processedLine);
            codeArea.moveTo(caretPosition);
        } else {
            // 处理选中的文本
            String processedText = processLines(selectedText, false);
            codeArea.replaceSelection(processedText);
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public String getName() {
        return "Hosts编辑器";
    }

    @Override
    public String getDescription() {
        return "编辑系统hosts文件的工具";
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public Node getPluginNode() {
        return rootPane;
    }

    @Override
    public void initialize() {
        // 初始化逻辑
    }

    @Override
    public void destroy() {
        // 清理逻辑
    }

    @Override
    public void setContent(String content) {
        codeArea.replaceText(content);
    }

    @Override
    public String getContent() {
        return codeArea.getText();
    }

    @Override
    public Image getIcon() {
        return IconGenerator.generateIcon(getName());
    }
}