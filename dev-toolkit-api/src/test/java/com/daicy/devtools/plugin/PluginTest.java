package com.daicy.devtools.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class PluginTest {
    private TestPlugin plugin;
    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        plugin = new TestPlugin();
    }

    @BeforeEach
    void setUp() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginLifecycle() {
        Platform.runLater(() -> {
            // 测试初始化
            assertFalse(plugin.isInitialized());
            plugin.initialize();
            assertTrue(plugin.isInitialized());
            
            // 测试销毁
            assertFalse(plugin.isDestroyed());
            plugin.destroy();
            assertTrue(plugin.isDestroyed());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginMetadata() {
        Platform.runLater(() -> {
            // 测试插件元数据
            assertEquals("Test Plugin", plugin.getName());
            assertEquals("A plugin for testing", plugin.getDescription());
            assertEquals("1.0.0", plugin.getVersion());
            assertEquals("Test Author", plugin.getAuthor());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginContent() {
        Platform.runLater(() -> {
            // 测试内容管理
            assertEquals("", plugin.getContent());
            
            String testContent = "Test Content";
            plugin.setContent(testContent);
            assertEquals(testContent, plugin.getContent());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginNode() {
        Platform.runLater(() -> {
            // 测试节点管理
            assertNull(plugin.getPluginNode());
            plugin.initialize();
            Node node = plugin.getPluginNode();
            assertNotNull(node);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginIcon() {
        Platform.runLater(() -> {
            // 测试图标生成
            Image icon = plugin.getIcon();
            assertNotNull(icon);
            assertEquals(32, (int)icon.getWidth());
            assertEquals(32, (int)icon.getHeight());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginStateTransitions() {
        Platform.runLater(() -> {
            // Test double initialization
            plugin.initialize();
            assertTrue(plugin.isInitialized());
            plugin.initialize(); // Should not throw or change state
            assertTrue(plugin.isInitialized());
            
            // Test operations after destruction
            plugin.destroy();
            assertTrue(plugin.isDestroyed());
            plugin.destroy(); // Should not throw or change state
            assertTrue(plugin.isDestroyed());
            
            // Test node access after destruction
            assertNotNull(plugin.getPluginNode()); // Node should still be accessible
            
            // Test content operations after destruction
            String testContent = "Post-destruction content";
            plugin.setContent(testContent);
            assertEquals(testContent, plugin.getContent()); // Content operations should work
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testConcurrentContentAccess() {
        Platform.runLater(() -> {
            // Simulate concurrent access to content
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    plugin.setContent("Content " + i);
                }
            });
            
            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    plugin.getContent();
                }
            });
            
            t1.start();
            t2.start();
            
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                fail("Concurrent access test interrupted");
            }
            
            // Verify plugin is still in valid state
            assertNotNull(plugin.getContent());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testPluginReinitialization() {
        Platform.runLater(() -> {
            // Initialize plugin
            plugin.initialize();
            assertTrue(plugin.isInitialized());
            Node firstNode = plugin.getPluginNode();
            
            // Destroy plugin
            plugin.destroy();
            assertTrue(plugin.isDestroyed());
            
            // Reinitialize plugin
            plugin = new TestPlugin();
            assertFalse(plugin.isInitialized());
            plugin.initialize();
            assertTrue(plugin.isInitialized());
            
            // Verify new instance is working
            Node newNode = plugin.getPluginNode();
            assertNotNull(newNode);
            assertNotSame(firstNode, newNode);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testContentPersistence() {
        Platform.runLater(() -> {
            // Test content persistence across plugin lifecycle
            String testContent = "Persistent content";
            plugin.setContent(testContent);
            
            plugin.initialize();
            assertEquals(testContent, plugin.getContent());
            
            plugin.destroy();
            assertEquals(testContent, plugin.getContent());
            
            // Test content operations in various states
            plugin.setContent("New content");
            assertEquals("New content", plugin.getContent());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
} 