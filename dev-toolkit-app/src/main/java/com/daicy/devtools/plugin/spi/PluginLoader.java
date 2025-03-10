package com.daicy.devtools.plugin.spi;

import com.daicy.devtools.plugin.Plugin;

import java.util.List;

/**
 * 插件加载器接口
 * 定义了插件加载的标准接口
 */
public interface PluginLoader {
    /**
     * 加载所有可用的插件
     * @return 插件列表
     */
    List<Plugin> loadPlugins();
    
    /**
     * 获取加载器名称
     * @return 加载器名称
     */
    String getName();
    
    /**
     * 获取加载器描述
     * @return 加载器描述
     */
    String getDescription();
}