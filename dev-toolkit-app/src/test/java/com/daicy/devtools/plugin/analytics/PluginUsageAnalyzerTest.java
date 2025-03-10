package com.daicy.devtools.plugin.analytics;

import com.daicy.devtools.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PluginUsageAnalyzerTest {
    private PluginUsageAnalyzer analyzer;
    private TestPlugin testPlugin;
    private static final Path USAGE_DATA_PATH = Paths.get(System.getProperty("user.dir"), "config", "plugin-usage.json");

    @BeforeEach
    void setUp() {
        // 确保测试前删除已存在的使用数据文件
        try {
            Files.deleteIfExists(USAGE_DATA_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        analyzer = PluginUsageAnalyzer.getInstance();
        testPlugin = new TestPlugin("TestPlugin", "1.0.0");
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        analyzer.clearUsageData();
    }

    @Test
    void testPluginUsageRecording() {
        // 测试插件使用次数记录
        analyzer.recordPluginStart(testPlugin);
        
        Optional<PluginUsageData> usageData = analyzer.getPluginUsageData(testPlugin.getName());
        assertTrue(usageData.isPresent());
        assertEquals(1, usageData.get().getUsageCount());

        // 再次启动插件
        analyzer.recordPluginStart(testPlugin);
        usageData = analyzer.getPluginUsageData(testPlugin.getName());
        assertEquals(2, usageData.get().getUsageCount());
    }

    @Test
    void testPluginUsageDuration() throws InterruptedException {
        // 测试插件使用时长记录
        analyzer.recordPluginStart(testPlugin);
        
        // 模拟插件运行1秒
        Thread.sleep(1000);
        
        analyzer.recordPluginStop(testPlugin);
        
        Optional<PluginUsageData> usageData = analyzer.getPluginUsageData(testPlugin.getName());
        assertTrue(usageData.isPresent());
        assertTrue(usageData.get().getTotalUsageDuration().getSeconds() >= 1);
    }

    @Test
    void testUsageReport() {
        // 测试使用报告生成
        analyzer.recordPluginStart(testPlugin);
        analyzer.recordPluginStop(testPlugin);

        List<PluginUsageReport> reports = analyzer.generateUsageReport();
        assertFalse(reports.isEmpty());
        assertEquals(testPlugin.getName(), reports.get(0).pluginName());
    }

    @Test
    void testUsageFrequency() {
        // 测试使用频率统计
        analyzer.recordPluginStart(testPlugin);
        analyzer.recordPluginStop(testPlugin);

        Map<String, Long> frequency = analyzer.getPluginUsageFrequency();
        assertTrue(frequency.containsKey(testPlugin.getName()));
        assertEquals(1L, frequency.get(testPlugin.getName()));
    }

    @Test
    void testDataPersistence() {
        // 测试数据持久化
        analyzer.recordPluginStart(testPlugin);
        analyzer.recordPluginStop(testPlugin);

        // 验证数据文件是否被创建
        assertTrue(Files.exists(USAGE_DATA_PATH));

        // 创建新的分析器实例，验证数据是否被正确加载
        PluginUsageAnalyzer newAnalyzer = PluginUsageAnalyzer.getInstance();
        Optional<PluginUsageData> usageData = newAnalyzer.getPluginUsageData(testPlugin.getName());
        assertTrue(usageData.isPresent());
        assertEquals(1, usageData.get().getUsageCount());
    }

    // 测试用的插件实现类
    private static class TestPlugin implements Plugin {
        private final String name;
        private final String version;

        public TestPlugin(String name, String version) {
            this.name = name;
            this.version = version;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getDescription() {
            return "Test Plugin";
        }

        @Override
        public String getAuthor() {
            return "Test Author";
        }

        @Override
        public String getContent() {
            return "Test Content";
        }

        @Override
        public void initialize() {
            // 测试用，无需实现
        }

        @Override
        public void setContent(String content) {
            // 测试用，无需实现
        }

        @Override
        public void destroy() {
            // 测试用，无需实现
        }

        @Override
        public javafx.scene.Node getPluginNode() {
            return null; // 测试用，返回null
        }
    }
}