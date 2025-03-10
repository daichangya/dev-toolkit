package com.daicy.devtools.plugin.markdown;

import com.daicy.devtools.plugin.Plugin;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.scene.layout.VBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Markdown编辑器插件
 */
public class MarkdownEditorPlugin implements Plugin {
    private VBox editorContainer;
    private TextArea editor;
    private WebView preview;
    private Parser parser;
    private HtmlRenderer renderer;

    public MarkdownEditorPlugin() {
        // 默认构造函数
    }

    @Override
    public void initialize() {
        // 初始化Markdown解析器
        MutableDataSet options = new MutableDataSet();
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();

        // 创建编辑器界面
        editorContainer = new VBox();
        
        // 工具栏
        ToolBar toolbar = new ToolBar();
        Button previewButton = new Button("预览");
        Button copyHtmlButton = new Button("复制HTML");
        toolbar.getItems().addAll(previewButton, copyHtmlButton);

        // 编辑器和预览区域
        SplitPane splitPane = new SplitPane();
        editor = new TextArea();
        preview = new WebView();
        
        splitPane.getItems().addAll(editor, preview);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        editorContainer.getChildren().addAll(toolbar, splitPane);

        // 添加预览按钮事件处理
        previewButton.setOnAction(e -> updatePreview());
        
        // 添加复制HTML按钮事件处理
        copyHtmlButton.setOnAction(e -> copyHtmlToClipboard());
    }

    @Override
    public String getName() {
        return "Markdown编辑器";
    }

    @Override
    public String getDescription() {
        return "A plugin for editing and previewing Markdown files";
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
    public javafx.scene.Node getPluginNode() {
        return editorContainer;
    }

    @Override
    public void setContent(String content) {
        editor.setText(content);
        updatePreview();
    }

    @Override
    public String getContent() {
        return editor.getText();
    }


    private void updatePreview() {
        String markdown = editor.getText();
        String html = renderer.render(parser.parse(markdown));
        preview.getEngine().loadContent(html);
    }

    private void copyHtmlToClipboard() {
        String markdown = editor.getText();
        String html = renderer.render(parser.parse(markdown));
        
        // 获取系统剪贴板
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putHtml(html);
        content.putString(html);  // 同时添加纯文本格式
        clipboard.setContent(content);

        // 显示成功提示
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText("HTML已成功复制到剪贴板");
        alert.showAndWait();
    }
}