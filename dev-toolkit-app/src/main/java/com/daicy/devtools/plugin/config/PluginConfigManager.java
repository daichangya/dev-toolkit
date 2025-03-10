package com.daicy.devtools.plugin.config;

import com.daicy.devtools.plugin.utils.LogManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.daicy.devtools.plugin.utils.ConfigException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PluginConfigManager {
    private final Path configPath;
    private final ObjectMapper objectMapper;
    private List<PluginConfig> pluginConfigs;
    public static final  Path  pluginsDir = Paths.get(System.getProperty("user.dir"), "plugins");

    public PluginConfigManager() {
        this.configPath = pluginsDir.resolve("plugin-config.json");
        this.objectMapper = new ObjectMapper();
        this.pluginConfigs = new ArrayList<>();
        loadConfigs();
    }

    // 使用静态内部类实现单例模式
    private static class SingletonHolder {
        private static final PluginConfigManager instance = new PluginConfigManager();
    }

    public static PluginConfigManager getInstance() {
        return PluginConfigManager.SingletonHolder.instance;
    }

    private final Logger logger = LogManager.getLogger(PluginConfigManager.class);

    private void loadConfigs() {
        if (Files.exists(configPath)) {
            try {
                CollectionType type = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, PluginConfig.class);
                pluginConfigs = objectMapper.readValue(configPath.toFile(), type);
                logger.info("成功加载插件配置文件");
            } catch (IOException e) {
                // 如果读取失败，使用空列表并抛出ConfigException
                pluginConfigs = new ArrayList<>();
                logger.severe("加载插件配置文件失败: " + e.getMessage());
                throw new ConfigException("加载插件配置文件失败", e);
            }
        } else {
            logger.info("插件配置文件不存在，使用空配置");
        }
    }

    public void saveConfigs() {
        try {
            objectMapper.writeValue(configPath.toFile(), pluginConfigs);
            logger.info("成功保存插件配置文件");
        } catch (IOException e) {
            logger.severe("保存插件配置文件失败: " + e.getMessage());
            throw new ConfigException("保存插件配置文件失败", e);
        }
    }

    public void addPluginConfig(PluginConfig config) {
        // 如果已存在相同ID的配置，则更新它
        Optional<PluginConfig> existingConfig = getPluginConfig(config.getName());
        if (existingConfig.isPresent()) {
            pluginConfigs.remove(existingConfig.get());
            logger.info("更新插件配置: " + config.getName());
        } else {
            logger.info("添加新插件配置: " + config.getName());
        }
        pluginConfigs.add(config);
        saveConfigs();
    }

    public void removePluginConfig(String name) {
        boolean removed = pluginConfigs.removeIf(config -> config.getName().equals(name));
        if (removed) {
            logger.info("移除插件配置: " + name);
            saveConfigs();
        } else {
            logger.warning("尝试移除不存在的插件配置: " + name);
        }
    }

    public void setPluginEnabled(String pluginId, boolean enabled) {
        getPluginConfig(pluginId).ifPresent(config -> {
            config.setEnabled(enabled);
            logger.info(String.format("设置插件 %s 状态为: %s", pluginId, enabled ? "启用" : "禁用"));
            saveConfigs();
        });
    }

    public boolean isPluginEnabled(String name) {
        return getPluginConfig(name)
                .map(PluginConfig::isEnabled)
                .orElse(true);
    }

    public Optional<PluginConfig> getPluginConfig(String name) {
        return pluginConfigs.stream()
                .filter(config -> config.getName().equals(name))
                .findFirst();
    }

    public List<PluginConfig> getAllConfigs() {
        return new ArrayList<>(pluginConfigs);
    }
}