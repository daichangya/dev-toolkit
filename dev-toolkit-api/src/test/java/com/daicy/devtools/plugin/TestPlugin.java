package com.daicy.devtools.plugin;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * 用于测试的插件实现
 */
public class TestPlugin implements Plugin {
    private String content = "";
    private Label node;
    private boolean isInitialized = false;
    private boolean isDestroyed = false;

    @Override
    public void initialize() {
        node = new Label("Test Plugin");
        isInitialized = true;
    }

    @Override
    public void destroy() {
        isDestroyed = true;
    }

    @Override
    public String getName() {
        return "Test Plugin";
    }

    @Override
    public String getDescription() {
        return "A plugin for testing";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getAuthor() {
        return "Test Author";
    }

    @Override
    public Node getPluginNode() {
        return node;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    // 测试辅助方法
    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
} 