package com.daicy.devtools.plugin.spi;

import com.daicy.devtools.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 基于Java SPI机制的插件加载器实现
 */
public class ServiceLoaderPluginLoader implements PluginLoader {
    @Override
    public List<Plugin> loadPlugins() {
        List<Plugin> plugins = new ArrayList<>();
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class);
        for (Plugin plugin : serviceLoader) {
            plugins.add(plugin);
        }
        return plugins;
    }

    @Override
    public String getName() {
        return "ServiceLoader Plugin Loader";
    }

    @Override
    public String getDescription() {
        return "使用Java SPI机制加载插件";
    }
}