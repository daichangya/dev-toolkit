package com.daicy.devtools.plugin.market;

import com.daicy.devtools.plugin.Plugin;
import com.daicy.devtools.plugin.config.PluginConfig;
import com.daicy.devtools.plugin.config.PluginConfigManager;
import com.daicy.devtools.plugin.spi.JarPluginLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 插件市场服务
 * 负责管理插件的元数据、本地插件目录和远程插件列表
 */
public class PluginMarketService {
    private final PluginConfigManager configManager;


    public PluginMarketService() {
        this.configManager = PluginConfigManager.getInstance();
    }

    /**
     * 安装本地插件
     */
    public CompletableFuture<Void> installLocalPlugin(File selectedFile) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 加载插件并验证
                List<Plugin> pluginList = JarPluginLoader.getInstance().loadPluginsFromJar(selectedFile.getAbsolutePath());
                if (pluginList.isEmpty()) {
                    throw new IllegalArgumentException("无效的插件文件：未找到有效的插件实现");
                }

                // 更新插件配置
                pluginList.forEach(plugin -> {
                    if (plugin instanceof Plugin) {
                        PluginConfig config = new PluginConfig();
                        config.setName(plugin.getName());
                        config.setDescription(plugin.getDescription());
                        config.setVersion(plugin.getVersion());
                        config.setAuthor(plugin.getAuthor());
                        config.setEnabled(true);
                        configManager.addPluginConfig(config);
                    }
                });

                // 构建目标文件路径
                Path targetPath = Paths.get(PluginConfigManager.pluginsDir.toString(), selectedFile.getName());

                // 确保目标目录存在
                Files.createDirectories(targetPath.getParent());

                // 检查目标文件是否存在，如果存在则先删除
                if (Files.exists(targetPath)) {
                    Files.delete(targetPath);
                }

                // 复制插件文件到目标目录
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println(selectedFile.getName() + " 插件已成功安装到 " + targetPath);
                
            } catch (IOException e) {
                String errorMessage = "安装插件失败：" + e.getMessage();
                System.err.println(errorMessage);
                throw new RuntimeException(errorMessage, e);
            } catch (Exception e) {
                String errorMessage = "安装插件过程中发生错误：" + e.getMessage();
                System.err.println(errorMessage);
                throw new RuntimeException(errorMessage, e);
            }
        });
    }

    /**
     * 安装插件
     */
    public CompletableFuture<Void> installPlugin(PluginConfig plugin) {
        return CompletableFuture.runAsync(() -> {
            // TODO: 实现插件下载和安装逻辑
            plugin.setEnabled(true);
        });
    }

    /**
     * 启用插件
     */
    public void enablePlugin(PluginConfig plugin) {
        plugin.setEnabled(true);
        configManager.setPluginEnabled(plugin.getName(), true);
    }

    /**
     * 禁用插件
     */
    public void disablePlugin(PluginConfig plugin) {
        plugin.setEnabled(false);
        configManager.setPluginEnabled(plugin.getName(), false);
    }

    /**
     * 卸载插件
     */
    public CompletableFuture<Void> uninstallPlugin(PluginConfig plugin) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("正在卸载插件: " + plugin.getName());
            plugin.setEnabled(false);
            configManager.setPluginEnabled(plugin.getName(), false);
        });
    }
}