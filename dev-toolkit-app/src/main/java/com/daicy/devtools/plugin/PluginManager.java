package com.daicy.devtools.plugin;

import com.daicy.devtools.plugin.config.PluginConfig;
import com.daicy.devtools.plugin.config.PluginConfigManager;
import com.daicy.devtools.plugin.market.PluginMarketService;
import com.daicy.devtools.plugin.spi.JarPluginLoader;
import com.daicy.devtools.plugin.spi.PluginLoader;
import com.daicy.devtools.plugin.spi.ServiceLoaderPluginLoader;
import com.daicy.devtools.plugin.utils.PluginException;
import com.daicy.devtools.plugin.analytics.PluginUsageAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件管理器
 * 负责管理所有插件的生命周期
 */
public class PluginManager {
    private final List<Plugin> plugins;
    private final List<PluginLoader> pluginLoaders;
    private final PluginMarketService marketService;
    private final PluginUsageAnalyzer usageAnalyzer;
    private static boolean isInitialized = false;

    protected PluginManager() {
        this.plugins = new ArrayList<>();
        this.pluginLoaders = new ArrayList<>();
        this.marketService = new PluginMarketService();
        this.usageAnalyzer = PluginUsageAnalyzer.getInstance();
        // 添加默认的插件加载器
        this.pluginLoaders.add(new ServiceLoaderPluginLoader());
        // 添加Jar包插件加载器
        this.pluginLoaders.add(JarPluginLoader.getInstance());
        loadPlugins();
    }

    // 创建唯一实例
    private static class SingletonHolder {
        private static PluginManager INSTANCE;
        
        static {
            try {
                INSTANCE = new PluginManager();
                isInitialized = true;
            } catch (Exception e) {
                System.err.println("初始化插件管理器失败: " + e.getMessage());
                INSTANCE = null;
                isInitialized = false;
                throw new PluginException("初始化插件管理器失败", e);
            }
        }
    }
    
    public static PluginManager getInstance() {
        if (!isInitialized) {
            SingletonHolder.INSTANCE = new PluginManager();
            isInitialized = true;
        }
        return SingletonHolder.INSTANCE;
    }

    /**
     * 重置插件管理器实例（仅用于测试）
     */
    static void resetInstance() {
        isInitialized = false;
        SingletonHolder.INSTANCE = null;
    }

    /**
     * 注册插件
     * @param plugin 插件实例
     */
    public void registerPlugin(Plugin plugin) {
        if (plugin == null) {
            throw new PluginException("Cannot register null plugin");
        }
        if (plugins.stream().noneMatch(p -> p.getName().equals(plugin.getName()))) {            plugins.add(plugin);
            System.out.println("Registered plugin: " + plugin.getName());
            usageAnalyzer.recordPluginStart(plugin);
            try {
                plugin.initialize();
            } catch (Exception e) {
                plugins.remove(plugin);
                throw new PluginException("Failed to initialize plugin: " + plugin.getName(), e);
            }
        }
    }

    /**
     * 注销插件
     * @param plugin 插件实例
     */
    public void unregisterPlugin(Plugin plugin) {
        if (plugin != null) {
            try {
                usageAnalyzer.recordPluginStop(plugin);
                plugin.destroy();
                plugins.remove(plugin);
                System.out.println("成功卸载插件: " + plugin.getName());
                
                // 从配置中禁用插件
                PluginConfigManager.getInstance().setPluginEnabled(plugin.getName(), false);
            } catch (Exception e) {
                System.err.println("卸载插件时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载所有可用的插件
     */
    private void loadPlugins() {
        try {
            for (PluginLoader loader : pluginLoaders) {
                List<Plugin> discoveredPlugins = loader.loadPlugins();
                for (Plugin plugin : discoveredPlugins) {
                    if (!isPluginLoaded(plugin.getName())) {
                        registerPlugin(plugin);
                        System.out.println("成功加载插件: " + plugin.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("加载插件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取所有已注册的插件
     * @return 插件列表
     */
    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins);
    }

    /**
     * 根据名称查找插件
     * @param name 插件名称
     * @return 插件实例，如果未找到返回null
     */
    public Plugin findPlugin(String name) {
        return plugins.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查插件是否已经加载
     * @param pluginName 插件名称
     * @return 如果插件已加载返回true，否则返回false
     */
    private boolean isPluginLoaded(String pluginName) {
        return plugins.stream().anyMatch(p -> p.getName().equals(pluginName));
    }

    /**
     * 从插件市场安装插件
     * @param metadata 插件元数据
     * @return 是否安装成功
     */
    public boolean installPluginFromMarket(PluginConfig metadata) {
        try {
            marketService.installPlugin(metadata).thenRun(() -> {
                loadPlugins(); // 重新加载插件以包含新安装的插件
                System.out.println("插件市场安装完成: " + metadata.getName());
            });
            return true;
        } catch (Exception e) {
            System.err.println("从插件市场安装插件失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 卸载插件
     * @param metadata 插件元数据
     * @return 是否卸载成功
     */
    public boolean uninstallPluginFromMarket(PluginConfig metadata) {
        try {
            Plugin plugin = findPlugin(metadata.getName());
            if (plugin != null) {
                unregisterPlugin(plugin);
            }
            marketService.uninstallPlugin(metadata).thenRun(() -> {
                System.out.println("插件市场卸载完成: " + metadata.getName());
            });
            return true;
        } catch (Exception e) {
            System.err.println("从插件市场卸载插件失败: " + e.getMessage());
            return false;
        }
    }
}