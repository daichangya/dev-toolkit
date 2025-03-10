package com.daicy.devtools.plugin.impl;

import com.daicy.devtools.plugin.IconGenerator;
import com.daicy.devtools.plugin.Plugin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * 文字转图标插件
 */
public class TextIconPlugin implements Plugin {
    /** 预览面板的最大尺寸 */
    public static final double USE_PREF_SIZE = 500;
    /** 预览画布的默认尺寸 */
    private static final int DEFAULT_CANVAS_SIZE = 200;
    /** 预览画布的最小尺寸 */
    private static final int MIN_CANVAS_SIZE = 100;
    /** 字体大小的最小值 */
    private static final int MIN_FONT_SIZE = 24;
    /** 字体大小的最大值 */
    private static final int MAX_FONT_SIZE = 144;
    /** 字体大小的默认值 */
    private static final int DEFAULT_FONT_SIZE = 48;
    /** 字体大小的主刻度单位 */
    private static final int FONT_SIZE_MAJOR_TICK = 24;
    /** 字体大小的增量单位 */
    private static final int FONT_SIZE_INCREMENT = 12;
    /** 默认字体大小 */
    private static final int DEFAULT_TEXT_SIZE = 36;

    private VBox rootContainer;
    private TextField textInput;
    private ColorPicker colorPicker;
    private Slider fontSizeSlider;
    private StackPane previewPane;
    private Text previewText;

    public TextIconPlugin() {
        // 默认构造函数
    }

    @Override
    public void initialize() {
        rootContainer = new VBox(10);
        rootContainer.setPadding(new Insets(10));

        // 创建配置面板
        VBox configPanel = createConfigPanel();

        // 创建预览面板
        StackPane outerContainer = createPreviewPane();

        // 创建分割面板
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(configPanel, outerContainer);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // 设置分割面板的分割策略
        splitPane.setDividerPositions(0.3); // 设置分割位置为30%
        SplitPane.setResizableWithParent(configPanel, true);
        SplitPane.setResizableWithParent(outerContainer, false); // 禁止预览区域随窗口调整

        // 创建工具栏
        ToolBar toolbar = createToolbar();

        rootContainer.getChildren().addAll(toolbar, splitPane);
    }

    private VBox createConfigPanel() {
        VBox configPanel = new VBox(10);
        configPanel.setPadding(new Insets(10));
    
        // 文本输入
        Label textLabel = new Label("输入文字：");
        textInput = new TextField();
        textInput.setPromptText("请输入要转换的文字");
        textInput.textProperty().addListener((obs, old, newValue) -> updatePreview());
    
        // 字体选择
        Label fontLabel = new Label("选择字体：");
        ComboBox<String> fontComboBox = new ComboBox<>();
        fontComboBox.getItems().addAll(
            "微软雅黑",
            "宋体",
            "黑体",
            "Arial",
            "Times New Roman"
        );
        fontComboBox.setValue("微软雅黑");
        fontComboBox.setOnAction(e -> {
            previewText.setFont(Font.font(
                fontComboBox.getValue(),
                fontSizeSlider.getValue()
            ));
        });
    
        // 颜色选择
        Label colorLabel = new Label("选择颜色：");
        colorPicker = new ColorPicker(Color.DARKBLUE);
        colorPicker.setOnAction(e -> updatePreview());
    
        // 字体大小
        Label sizeLabel = new Label("字体大小：");
        fontSizeSlider = new Slider(MIN_FONT_SIZE, MAX_FONT_SIZE, DEFAULT_FONT_SIZE);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(FONT_SIZE_MAJOR_TICK);
        fontSizeSlider.setBlockIncrement(FONT_SIZE_INCREMENT);
        fontSizeSlider.valueProperty().addListener((obs, old, newValue) -> {
            previewText.setFont(Font.font(
                fontComboBox.getValue(),
                newValue.doubleValue()
            ));
        });
    
        // 画布宽度
        Label widthLabel = new Label("画布宽度：");
        TextField widthInput = new TextField("200");
        widthInput.setPrefWidth(100);
        widthInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {  // 当失去焦点时
                try {
                    int width = Integer.parseInt(widthInput.getText());
                    if (width >= MIN_CANVAS_SIZE && width <= USE_PREF_SIZE) {
                        previewPane.setPrefWidth(width);
                        previewPane.setMinWidth(width);
                        previewPane.setMaxWidth(width);
                        updatePreview();
                    } else {
                        widthInput.setText(String.valueOf(DEFAULT_CANVAS_SIZE));
                        previewPane.setPrefWidth(DEFAULT_CANVAS_SIZE);
                        previewPane.setMinWidth(DEFAULT_CANVAS_SIZE);
                        previewPane.setMaxWidth(DEFAULT_CANVAS_SIZE);
                        updatePreview();
                    }
                } catch (NumberFormatException e) {
                    widthInput.setText(String.valueOf(DEFAULT_CANVAS_SIZE));
                    previewPane.setPrefWidth(DEFAULT_CANVAS_SIZE);
                    updatePreview();
                }
            }
        });
    
        // 画布高度
        Label heightLabel = new Label("画布高度：");
        TextField heightInput = new TextField("200");
        heightInput.setPrefWidth(100);
        heightInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {  // 当失去焦点时
                try {
                    int height = Integer.parseInt(heightInput.getText());
                    if (height >= MIN_CANVAS_SIZE && height <= USE_PREF_SIZE) {
                        previewPane.setPrefHeight(height);
                        previewPane.setMinHeight(height);
                        previewPane.setMaxHeight(height);
                        updatePreview();
                    } else {
                        heightInput.setText(String.valueOf(DEFAULT_CANVAS_SIZE));
                        previewPane.setPrefHeight(DEFAULT_CANVAS_SIZE);
                        previewPane.setMinHeight(DEFAULT_CANVAS_SIZE);
                        previewPane.setMaxHeight(DEFAULT_CANVAS_SIZE);
                        updatePreview();
                    }
                } catch (NumberFormatException e) {
                    heightInput.setText(String.valueOf(DEFAULT_CANVAS_SIZE));
                    previewPane.setPrefHeight(DEFAULT_CANVAS_SIZE);
                    updatePreview();
                }
            }
        });
    
        // 背景颜色选择
        Label bgColorLabel = new Label("背景颜色：");
        ColorPicker bgColorPicker = new ColorPicker(Color.WHITE);
        bgColorPicker.setOnAction(e -> {
            String colorStyle = String.format("-fx-background-color: #%02X%02X%02X;",
                (int)(bgColorPicker.getValue().getRed() * 255),
                (int)(bgColorPicker.getValue().getGreen() * 255),
                (int)(bgColorPicker.getValue().getBlue() * 255));
            previewPane.setStyle(colorStyle);
            updatePreview();
        });
    
        configPanel.getChildren().addAll(
            textLabel, textInput,
            fontLabel, fontComboBox,
            colorLabel, colorPicker,
            sizeLabel, fontSizeSlider,
            widthLabel, widthInput,
            heightLabel, heightInput,
            bgColorLabel, bgColorPicker
        );
    
        return configPanel;
    }
    
    private StackPane createPreviewPane() {
        // 创建一个固定大小的外层容器
        StackPane outerContainer = new StackPane();
        outerContainer.setStyle("-fx-background-color: #f0f0f0;");
        // 设置固定大小，不允许改变
        outerContainer.setPrefSize(USE_PREF_SIZE, USE_PREF_SIZE);
        outerContainer.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        outerContainer.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        // 创建预览画布
        previewPane = new StackPane();
        previewPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        // 设置初始大小和最小大小
        previewPane.setPrefSize(200, 200);
        previewPane.setMinSize(100, 100);
        previewPane.setMaxSize(USE_PREF_SIZE - 40, USE_PREF_SIZE - 40); // 留出边距

        // 创建并配置预览文本
        previewText = new Text();
        previewText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        previewPane.getChildren().add(previewText);
        StackPane.setAlignment(previewText, Pos.CENTER);

        // 将预览画布添加到外层容器，并保持居中
        outerContainer.getChildren().add(previewPane);
        StackPane.setAlignment(previewPane, Pos.CENTER);

        return outerContainer;
    }


    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        
        Button exportPngButton = new Button("导出PNG");
        exportPngButton.setOnAction(e -> exportImage("png"));
        
        toolbar.getItems().addAll(exportPngButton);
        return toolbar;
    }

    private void updatePreview() {
        String text = textInput.getText();
        if (text == null || text.isEmpty()) {
            text = "预览";
        }
        previewText.setText(text);
        previewText.setFill(colorPicker.getValue());
        previewText.setFont(Font.font(fontSizeSlider.getValue()));
    }

    private void exportImage(String format) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图标");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG图片", "*.png")
        );

        File file = fileChooser.showSaveDialog(rootContainer.getScene().getWindow());
        if (file != null) {
            try {
                // 创建快照
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                
                // 设置导出图片的尺寸与预览画布一致
                WritableImage image = new WritableImage((int)previewPane.getWidth(), (int)previewPane.getHeight());
                previewPane.snapshot(params, image);

                // 保存图片
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, file);

                // 显示成功提示
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("成功");
                alert.setHeaderText(null);
                alert.setContentText("图标已成功导出");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText(null);
                alert.setContentText("导出图标失败：" + ex.getMessage());
                alert.showAndWait();
            }
        }
    }


    @Override
    public String getName() {
        return "文字图标生成器";
    }

    @Override
    public String getDescription() {
        return "将文字转换为自定义颜色和大小的图标";
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
        return rootContainer;
    }

    @Override
    public void setContent(String content) {
        textInput.setText(content);
        updatePreview();
    }

    @Override
    public Image getIcon() {
        return IconGenerator.generateIcon(getName());
    }

    @Override
    public String getContent() {
        return textInput.getText();
    }
}