package com.daicy.devtools.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Scene;
import com.daicy.devtools.plugin.utils.LogManager;
import java.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static final String LIGHT_THEME = "light";
    private static final String DARK_THEME = "dark";
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.dir"), "config", "theme.json");
    private final Logger logger = LogManager.getLogger(ThemeManager.class);
    
    private String currentTheme;
    private final ObjectMapper objectMapper;
    private final Map<String, String> themeStylesheets;
    
    private static class SingletonHolder {
        private static final ThemeManager instance = new ThemeManager();
    }
    
    public static ThemeManager getInstance() {
        return SingletonHolder.instance;
    }
    
    private ThemeManager() {
        this.objectMapper = new ObjectMapper();
        this.themeStylesheets = new HashMap<>();
        initializeThemes();
        loadThemeConfig();
    }
    
    private void initializeThemes() {
        themeStylesheets.put(LIGHT_THEME, "/styles/light-theme.css");
        themeStylesheets.put(DARK_THEME, "/styles/dark-theme.css");
        logger.info("主题样式表初始化完成");
    }
    
    private void loadThemeConfig() {
        try {
            if (CONFIG_PATH.toFile().exists()) {
                Map<String, String> config = objectMapper.readValue(CONFIG_PATH.toFile(), Map.class);
                currentTheme = config.getOrDefault("theme", LIGHT_THEME);
                logger.info("成功加载主题配置: " + currentTheme);
            } else {
                currentTheme = LIGHT_THEME;
                saveThemeConfig();
                logger.info("主题配置文件不存在，使用默认主题: " + currentTheme);
            }
        } catch (IOException e) {
            logger.severe("加载主题配置失败: " + e.getMessage());
            currentTheme = LIGHT_THEME;
        }
    }
    
    private void saveThemeConfig() {
        try {
            CONFIG_PATH.getParent().toFile().mkdirs();
            Map<String, String> config = new HashMap<>();
            config.put("theme", currentTheme);
            objectMapper.writeValue(CONFIG_PATH.toFile(), config);
            logger.info("成功保存主题配置: " + currentTheme);
        } catch (IOException e) {
            logger.severe("保存主题配置失败: " + e.getMessage());
        }
    }
    
    public void setTheme(Scene scene, String theme) {
        if (!themeStylesheets.containsKey(theme)) {
            logger.warning("尝试设置无效的主题: " + theme);
            return;
        }
        
        // 移除当前主题
        scene.getStylesheets().clear();
        
        // 添加新主题
        String stylesheetPath = themeStylesheets.get(theme);
        scene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());
        
        currentTheme = theme;
        saveThemeConfig();
        logger.info("主题切换成功: " + theme);
    }
    
    public void toggleTheme(Scene scene) {
        String newTheme = LIGHT_THEME.equals(currentTheme) ? DARK_THEME : LIGHT_THEME;
        setTheme(scene, newTheme);
        logger.info("主题切换: " + currentTheme + " -> " + newTheme);
    }
    
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    public boolean isDarkTheme() {
        return DARK_THEME.equals(currentTheme);
    }
}