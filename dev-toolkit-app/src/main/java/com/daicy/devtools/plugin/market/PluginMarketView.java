package com.daicy.devtools.plugin.market;

import com.daicy.devtools.plugin.config.PluginConfig;
import com.daicy.devtools.plugin.config.PluginConfigManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * 插件市场界面
 * 用于展示和管理插件
 */
public class PluginMarketView extends BorderPane {
    private final PluginMarketService marketService;
    private final ListView<PluginConfig> pluginListView;

    public PluginMarketView(PluginMarketService marketService) {
        this.marketService = marketService;
        this.pluginListView = new ListView<>();

        initializeUI();
        loadPlugins();
    }

    private void initializeUI() {
        // 创建顶部工具栏
        ToolBar toolBar = new ToolBar();
        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> loadPlugins());
        
        Button uploadButton = new Button("上传插件");
        uploadButton.setOnAction(e -> uploadPlugin());
        
        toolBar.getItems().addAll(refreshButton, uploadButton);
        setTop(toolBar);

        // 创建插件列表
        pluginListView.setCellFactory(param -> new ListCell<PluginConfig>() {
            @Override
            protected void updateItem(PluginConfig item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox(5);
                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-font-weight: bold");
                    Label descLabel = new Label(item.getDescription());
                    Label authorLabel = new Label("作者: " + item.getAuthor());
                    Label versionLabel = new Label("版本: " + item.getVersion());

                    HBox infoBox = new HBox(10);
                    infoBox.getChildren().addAll(authorLabel, versionLabel);

                    Button actionButton = new Button(item.isEnabled() ? "卸载" : "安装");
                    actionButton.setOnAction(e -> {
                        if (item.isEnabled()) {
                            uninstallPlugin(item);
                        } else {
                            installPlugin(item);
                        }
                    });

                    content.getChildren().addAll(nameLabel, descLabel, infoBox, actionButton);
                    content.setPadding(new Insets(5));
                    setGraphic(content);
                }
            }
        });

        setCenter(pluginListView);
    }

    private void loadPlugins() {
        javafx.application.Platform.runLater(() -> {
            PluginConfigManager configManager = PluginConfigManager.getInstance();
            pluginListView.getItems().clear();
            pluginListView.getItems().addAll(configManager.getAllConfigs());
        });
    }

    private void installPlugin(PluginConfig plugin) {
        marketService.installPlugin(plugin).thenRun(() -> {
            javafx.application.Platform.runLater(this::loadPlugins);
        });
    }

    private void uninstallPlugin(PluginConfig plugin) {
        marketService.uninstallPlugin(plugin).thenRun(() -> {
            javafx.application.Platform.runLater(this::loadPlugins);
        });
    }
    
    private void uploadPlugin() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择插件JAR包");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JAR文件", "*.jar")
        );
        
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            marketService.installLocalPlugin(selectedFile).thenRun(() -> {
                javafx.application.Platform.runLater(this::loadPlugins);
            });
        }
    }
}