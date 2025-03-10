package com.daicy.devtools.plugin;

import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 * 插件接口
 * 所有DevTools插件都必须实现此接口
 */
public interface Plugin {

    /**
     * 获取插件图标
     * @return 插件图标
     */
    default Image getIcon() {
        return IconGenerator.generateIcon(getName());
    }

    /**
     * 销毁插件资源
     */
    default void destroy() {}

    /**
     * 初始化插件
     * 在插件加载时调用此方法
     */
    void initialize();

    /**
     * 获取插件名称
     * @return 插件名称
     */
    String getName();

    /**
     * 获取插件描述
     * @return 插件描述
     */
    String getDescription();

    /**
     * 获取插件版本
     * @return 插件版本
     */
    String getVersion();

    /**
     * 获取插件作者
     * @return 插件作者
     */
    String getAuthor();

    /**
     * 获取插件的主界面节点
     * @return JavaFX节点
     */
    Node getPluginNode();

    /**
     * 设置插件内容
     * @param content 要设置的内容
     */
    void setContent(String content);

    /**
     * 获取插件内容
     * @return 插件内容
     */
    String getContent();
}