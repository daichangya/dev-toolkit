package com.daicy.devtools.plugin.spi;

import com.daicy.devtools.plugin.Plugin;
import com.daicy.devtools.plugin.config.PluginConfigManager;
import com.daicy.devtools.plugin.utils.PluginException;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class JarPluginLoader implements PluginLoader {
    private final String pluginDirectory;

    public JarPluginLoader() {
        this.pluginDirectory = PluginConfigManager.pluginsDir.toString();
    }


    // 使用静态内部类实现单例模式
    private static class SingletonHolder {
        private static final JarPluginLoader instance = new JarPluginLoader();
    }

    public static JarPluginLoader getInstance() {
        return JarPluginLoader.SingletonHolder.instance;
    }

    @Override
    public List<Plugin> loadPlugins() {
        List<Plugin> plugins = new ArrayList<>();
        File directory = new File(pluginDirectory);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("插件目录不存在或不是一个目录: " + pluginDirectory);
            throw new PluginException("插件目录不存在或不是一个目录: " + pluginDirectory);
        }

        File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles != null) {
            for (File jarFile : jarFiles) {
                System.out.println("正在加载插件: " + jarFile.getName());
                plugins.addAll(loadPluginsFromJar(jarFile.getAbsolutePath()));
            }
        }

        return plugins;
    }

    @Override
    public String getName() {
        return "Jar Plugin Loader";
    }

    @Override
    public String getDescription() {
        return "从外部jar包加载插件";
    }

    public List<Plugin> loadPluginsFromJar(String jarPath) {
        List<Plugin> plugins = new ArrayList<>();
        try {
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{new File(jarPath).toURI().toURL()},
                    Thread.currentThread().getContextClassLoader()
            );

            try (JarFile jarFile = new JarFile(jarPath)) {
                jarFile.stream()
                        .filter(entry -> entry.getName().endsWith(".class"))
                        .filter(entry -> !entry.getName().equals("module-info.class"))
                        .filter(entry -> !entry.getName().startsWith("META-INF/versions/"))
                        .forEach(entry -> {
                            String className = entry.getName().replace("/", ".").replace(".class", "");
                            try {
                                Class<?> clazz = classLoader.loadClass(className);
                                if (Plugin.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                                    Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
                                    System.out.println("加载插件: " + className);
                                    plugins.add(plugin);
                                }
                            } catch (Exception e) {
                                // Skip classes that can't be loaded
                                System.out.println("无法加载类: " + className + ", 原因: " + e.getMessage());
                                throw new PluginException("无法加载类: " + className, e);
                            }
                        });
            }
        } catch (Exception e) {
            System.err.println("加载插件jar文件时发生错误: " + e.getMessage());
            throw new PluginException("加载插件jar文件时发生错误", e);
        }
        return plugins;
    }
}