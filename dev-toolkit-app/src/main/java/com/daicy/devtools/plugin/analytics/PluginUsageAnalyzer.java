package com.daicy.devtools.plugin.analytics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.daicy.devtools.plugin.Plugin;
import com.daicy.devtools.plugin.config.JacksonConfig;
import com.daicy.devtools.plugin.utils.LogManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public class PluginUsageAnalyzer {
    private static final Path USAGE_DATA_PATH = Paths.get(System.getProperty("user.dir"), "config", "plugin-usage.json");
    private final ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
    private final Map<String, PluginUsageData> usageDataMap;
    private final Map<String, LocalDateTime> activePlugins;
    private final Logger logger;

    private static class SingletonHolder {
        private static final PluginUsageAnalyzer instance = new PluginUsageAnalyzer();
    }

    public static PluginUsageAnalyzer getInstance() {
        return SingletonHolder.instance;
    }

    private PluginUsageAnalyzer() {
        this.usageDataMap = new ConcurrentHashMap<>();
        this.activePlugins = new ConcurrentHashMap<>();
        this.logger = LogManager.getLogger(PluginUsageAnalyzer.class);
        loadUsageData();
    }

    private void loadUsageData() {
        if (Files.exists(USAGE_DATA_PATH)) {
            try {
                CollectionType type = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, PluginUsageData.class);
                List<PluginUsageData> dataList = objectMapper.readValue(USAGE_DATA_PATH.toFile(), type);
                dataList.forEach(data -> usageDataMap.put(data.getPluginName(), data));
                logger.info("成功加载插件使用数据");
            } catch (IOException e) {
                logger.severe("加载插件使用数据失败: " + e.getMessage());
            }
        }
    }

    private void saveUsageData() {
        try {
            Files.createDirectories(USAGE_DATA_PATH.getParent());
            objectMapper.writeValue(USAGE_DATA_PATH.toFile(), new ArrayList<>(usageDataMap.values()));
            logger.info("成功保存插件使用数据");
        } catch (IOException e) {
            logger.severe("保存插件使用数据失败: " + e.getMessage());
        }
    }

    public void recordPluginStart(Plugin plugin) {
        String pluginName = plugin.getName();
        activePlugins.put(pluginName, LocalDateTime.now());
        PluginUsageData data = usageDataMap.computeIfAbsent(pluginName, 
            k -> new PluginUsageData(pluginName));
        data.incrementUsageCount();
        saveUsageData();
        logger.info("记录插件启动: " + pluginName);
    }

    public void recordPluginStop(Plugin plugin) {
        String pluginName = plugin.getName();
        LocalDateTime startTime = activePlugins.remove(pluginName);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            PluginUsageData data = usageDataMap.get(pluginName);
            if (data != null) {
                data.addUsageDuration(duration);
                data.setLastUsed(LocalDateTime.now());
                saveUsageData();
                logger.info(String.format("记录插件停止: %s, 使用时长: %d分钟", 
                    pluginName, duration.toMinutes()));
            }
        }
    }

    public List<PluginUsageReport> generateUsageReport() {
        return usageDataMap.values().stream()
                .map(data -> new PluginUsageReport(
                    data.getPluginName(),
                    data.getUsageCount(),
                    data.getTotalUsageDuration().toMinutes(),
                    data.getLastUsed()
                ))
                .sorted(Comparator.comparing(PluginUsageReport::usageCount).reversed())
                .collect(Collectors.toList());
    }

    public Map<String, Long> getPluginUsageFrequency() {
        return usageDataMap.values().stream()
                .collect(Collectors.toMap(
                    PluginUsageData::getPluginName,
                    PluginUsageData::getUsageCount
                ));
    }

    public Optional<PluginUsageData> getPluginUsageData(String pluginName) {
        return Optional.ofNullable(usageDataMap.get(pluginName));
    }

    public void clearUsageData() {
        usageDataMap.clear();
        activePlugins.clear();
        saveUsageData();
        logger.info("清除所有插件使用数据");
    }
}