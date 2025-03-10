package com.daicy.devtools.plugin;

import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图标生成工具类
 */
public class IconGenerator {
    private static final int ICON_SIZE = 32;
    private static final double FONT_RATIO = 0.7; // 增大字体比例
    private static final Color BACKGROUND_COLOR = Color.web("#4A90E2"); // 使用更柔和的蓝色
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final double CORNER_RADIUS = 8.0; // 圆角半径

    /**
     * 为插件生成默认图标
     * @param text 图标文字（通常是插件名称的第一个字）
     * @return 生成的图标
     */
    public static Image generateIcon(String text) {
        // 创建文本节点
        Text textNode = new Text();
        textNode.setText(text.substring(0, 1)); // 使用第一个字符
        textNode.setFont(Font.font("System", FontWeight.BOLD, ICON_SIZE * FONT_RATIO));
        textNode.setFill(TEXT_COLOR);

        // 创建圆角矩形背景
        javafx.scene.shape.Rectangle background = new javafx.scene.shape.Rectangle(
            ICON_SIZE, ICON_SIZE,
            BACKGROUND_COLOR
        );
        background.setArcWidth(CORNER_RADIUS * 2);
        background.setArcHeight(CORNER_RADIUS * 2);

        // 创建容器并添加背景和文本
        StackPane container = new StackPane();
        container.getChildren().addAll(background, textNode);
        container.setAlignment(Pos.CENTER); // 确保文本居中

        // 设置快照参数
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // 创建图标
        WritableImage icon = new WritableImage(ICON_SIZE, ICON_SIZE);
        container.snapshot(params, icon);

        return icon;
    }
}