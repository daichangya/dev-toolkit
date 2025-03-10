package com.daicy.devtools;

import com.daicy.devtools.plugin.Plugin;
import com.daicy.devtools.plugin.PluginManager;
import com.daicy.devtools.plugin.config.PluginConfig;
import com.daicy.devtools.plugin.config.PluginConfigManager;
import com.daicy.devtools.plugin.config.ThemeManager;
import com.daicy.devtools.plugin.impl.Base64Plugin;
import com.daicy.devtools.plugin.impl.HostEditorPlugin;
import com.daicy.devtools.plugin.impl.JsonToBeanPlugin;
import com.daicy.devtools.plugin.market.PluginMarketService;
import com.daicy.devtools.plugin.market.PluginMarketView;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class MainApp extends Application {
    private Stage primaryStage;
    private List<Plugin> plugins;
    private Plugin currentPlugin;
    private PluginManager pluginManager;
    private PluginConfigManager configManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.plugins = new ArrayList<>();
        initializePlugins();

        BorderPane root = new BorderPane();

        // 创建菜单栏
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // 创建左侧插件列表
        FlowPane pluginFlow = new FlowPane();
        pluginFlow.setPrefWidth(50);
        pluginFlow.setVgap(10);
        pluginFlow.setHgap(10);
        pluginFlow.setPadding(new Insets(10));

        // 创建标签页面板
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // 为每个插件创建按钮
        plugins.forEach(plugin -> {
            Button pluginButton = new Button();
            ImageView iconView = new ImageView(plugin.getIcon());
            iconView.setFitHeight(32);
            iconView.setFitWidth(32);
            pluginButton.setGraphic(iconView);
            pluginButton.getStyleClass().add("plugin-button");
            
            // 添加Tooltip显示插件名称
            Tooltip tooltip = new Tooltip(plugin.getName());
            pluginButton.setTooltip(tooltip);

            // 设置按钮点击事件
            pluginButton.setOnAction(event -> switchPlugin(plugin, tabPane));

            pluginFlow.getChildren().add(pluginButton);
        });

        VBox leftPanel = new VBox();
        leftPanel.getChildren().addAll(pluginFlow);
        root.setLeft(leftPanel);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 800, 600);
        // 应用当前主题
        ThemeManager.getInstance().setTheme(scene, ThemeManager.getInstance().getCurrentTheme());
        primaryStage.setTitle("开发工具集");
        primaryStage.setScene(scene);
        // 设置窗口默认最大化
        primaryStage.setMaximized(true);
        primaryStage.show();

        // 创建并添加主页标签
        Tab homeTab = new Tab("主页");
        homeTab.setClosable(false);  // 设置主页标签不可关闭
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        // 添加错误处理监听器
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.FAILED) {
                // 加载本地README文件
                try {
                    String readmePath = System.getProperty("user.dir") + "/README.md";
                    String readmeContent = Files.readString(Path.of(readmePath));
                    // 将Markdown内容转换为HTML
                    String htmlContent = String.format(
                        "<html><body style='font-family: Arial, sans-serif; margin: 20px;'><pre>%s</pre></body></html>",
                        readmeContent.replace("<", "&lt;").replace(">", "&gt;")
                    );
                    webEngine.loadContent(htmlContent, "text/html");
                } catch (Exception e) {
                    webEngine.loadContent(
                        "<html><body style='font-family: Arial, sans-serif; margin: 20px;'><h1>无法加载内容</h1><p>请检查网络连接或确保README.md文件存在。</p></body></html>",
                        "text/html"
                    );
                }
            }
        });
        
        // 尝试加载在线内容
        webEngine.load("https://zthinker.com");
        homeTab.setContent(webView);
        tabPane.getTabs().add(homeTab);
        
        // 默认选中主页标签
        tabPane.getSelectionModel().select(homeTab);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // 文件菜单
        Menu fileMenu = new Menu("文件");
        MenuItem openItem = new MenuItem("打开");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        openItem.setOnAction(e -> openFile());
        
        MenuItem saveItem = new MenuItem("保存");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        saveItem.setOnAction(e -> saveFile());
        
        MenuItem exitItem = new MenuItem("退出");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(), exitItem);

        // 插件菜单
        Menu pluginMenu = new Menu("插件");
        MenuItem marketItem = new MenuItem("插件市场");
        marketItem.setOnAction(e -> openPluginMarket());
        MenuItem usageReportItem = new MenuItem("使用报告");
        usageReportItem.setOnAction(e -> showUsageReport());
        pluginMenu.getItems().addAll(marketItem, new SeparatorMenuItem(), usageReportItem);

        // 视图菜单
        Menu viewMenu = new Menu("视图");
        MenuItem lightThemeItem = new MenuItem("浅色主题");
        MenuItem darkThemeItem = new MenuItem("深色主题");
        
        lightThemeItem.setOnAction(e -> ThemeManager.getInstance().setTheme(primaryStage.getScene(), "light"));
        darkThemeItem.setOnAction(e -> ThemeManager.getInstance().setTheme(primaryStage.getScene(), "dark"));
        
        viewMenu.getItems().addAll(lightThemeItem, darkThemeItem);

        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("关于");
            alert.setHeaderText("开发工具集");
            alert.setContentText("版本: 1.0\n作者: DevTools Team\n\n一个可扩展的开发工具集合，支持插件化开发。");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, pluginMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private void showUsageReport() {
        Stage reportStage = new Stage();
        reportStage.setTitle("插件使用报告");

        TableView<com.daicy.devtools.plugin.analytics.PluginUsageReport> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle("-fx-font-size: 14px;");

        // 创建列
        TableColumn<com.daicy.devtools.plugin.analytics.PluginUsageReport, String> nameColumn = 
            new TableColumn<>("插件名称");
        nameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().pluginName()));
        nameColumn.setSortable(true);

        TableColumn<com.daicy.devtools.plugin.analytics.PluginUsageReport, Number> countColumn = 
            new TableColumn<>("使用次数");
        countColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleLongProperty(data.getValue().usageCount()));
        countColumn.setSortable(true);
        countColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<com.daicy.devtools.plugin.analytics.PluginUsageReport, Number> durationColumn = 
            new TableColumn<>("总使用时长(分钟)");
        durationColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleLongProperty(data.getValue().totalUsageMinutes()));
        durationColumn.setSortable(true);
        durationColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<com.daicy.devtools.plugin.analytics.PluginUsageReport, String> lastUsedColumn = 
            new TableColumn<>("最后使用时间");
        lastUsedColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().lastUsed().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        lastUsedColumn.setSortable(true);
        lastUsedColumn.setStyle("-fx-alignment: CENTER;");

        tableView.getColumns().addAll(nameColumn, countColumn, durationColumn, lastUsedColumn);

        // 设置默认排序
        tableView.getSortOrder().add(countColumn);
        countColumn.setSortType(TableColumn.SortType.DESCENDING);

        // 设置数据
        tableView.getItems().addAll(
            com.daicy.devtools.plugin.analytics.PluginUsageAnalyzer.getInstance().generateUsageReport());
        
        // 自动调整列宽
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // 创建布局
        VBox vbox = new VBox(tableView);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 600, 400);
        reportStage.setScene(scene);
        reportStage.show();
    }

    private void openPluginMarket() {
        Stage marketStage = new Stage();
        marketStage.setTitle("插件市场");
        
        PluginMarketService marketService = new PluginMarketService();
        PluginMarketView marketView = new PluginMarketView(marketService);
        
        Scene scene = new Scene(marketView, 600, 400);
        marketStage.setScene(scene);
        marketStage.show();
    }

    private void initializePlugins() {
        pluginManager = PluginManager.getInstance();
        configManager = new PluginConfigManager();
        
        // 注册内置插件
        registerPlugin(new Base64Plugin());
        registerPlugin(new HostEditorPlugin());
        registerPlugin(new JsonToBeanPlugin());
        
        // 加载已安装的插件
        plugins = pluginManager.getPlugins().stream()
                .filter(plugin -> configManager.isPluginEnabled(plugin.getName()))
                .toList();
    }
    
    private void registerPlugin(Plugin plugin) {
        pluginManager.registerPlugin(plugin);
        
        // 创建并保存插件配置
        PluginConfig config = new PluginConfig();
        config.setName(plugin.getName());
        config.setEnabled(true);
        configManager.addPluginConfig(config);
    }

    private void switchPlugin(Plugin plugin, TabPane tabPane) {
        if (plugin == null) return;

        // 检查是否已存在相同名称的标签页
        Tab existingTab = null;
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(plugin.getName())) {
                existingTab = tab;
                break;
            }
        }

        if (existingTab != null) {
            tabPane.getSelectionModel().select(existingTab);
        } else {
            Tab tab = new Tab(plugin.getName());
            tab.setContent(plugin.getPluginNode());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            
            // 记录插件启动
            com.daicy.devtools.plugin.analytics.PluginUsageAnalyzer.getInstance().recordPluginStart(plugin);
            
            // 添加标签页关闭事件监听
            tab.setOnClosed(event -> {
                com.daicy.devtools.plugin.analytics.PluginUsageAnalyzer.getInstance().recordPluginStop(plugin);
            });
        }

        // 更新当前插件引用
        currentPlugin = plugin;
    }

    private void openFile() {
        if (currentPlugin != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("打开文件");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    String content = Files.readString(file.toPath());
                    currentPlugin.setContent(content);
                } catch (Exception e) {
                    showError("错误", "无法打开文件：" + e.getMessage());
                }
            }
        }
    }

    private void saveFile() {
        if (currentPlugin != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存文件");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    String content = currentPlugin.getContent();
                    Files.writeString(file.toPath(), content);
                } catch (Exception e) {
                    showError("错误", "无法保存文件：" + e.getMessage());
                }
            }
        }
    }

    private void saveFileAs() {
        saveFile(); // 直接调用saveFile方法，因为功能相同
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}