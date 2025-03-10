package com.daicy.devtools.plugin.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 统一的文件操作工具类
 */
public class FileUtils {
    private static final FileChooser fileChooser = new FileChooser();

    /**
     * 打开文件并读取内容
     *
     * @param window 父窗口
     * @param title 文件选择器标题
     * @param extensions 文件扩展名过滤器，格式为："*.txt", "*.json" 等
     * @return 文件内容，如果用户取消选择则返回null
     */
    public static String openFile(Window window, String title, String... extensions) {
        configureFileChooser(title, extensions);
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            try {
                return Files.readString(file.toPath());
            } catch (IOException e) {
                ExceptionHandler.handleConfigException(e, "读取文件失败");
                return null;
            }
        }
        return null;
    }

    /**
     * 保存内容到文件
     *
     * @param window 父窗口
     * @param content 要保存的内容
     * @param title 文件选择器标题
     * @param extensions 文件扩展名过滤器，格式为："*.txt", "*.json" 等
     * @return 是否保存成功
     */
    public static boolean saveFile(Window window, String content, String title, String... extensions) {
        configureFileChooser(title, extensions);
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), content);
                return true;
            } catch (IOException e) {
                ExceptionHandler.handleConfigException(e, "保存文件失败");
                return false;
            }
        }
        return false;
    }

    /**
     * 配置文件选择器
     */
    private static void configureFileChooser(String title, String... extensions) {
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().clear();
        if (extensions != null && extensions.length > 0) {
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                "支持的文件类型", List.of(extensions));
            fileChooser.getExtensionFilters().add(filter);
        }
    }

    /**
     * 测试文件操作异常处理
     */
    public static void testExceptionHandling() {
        // 测试读取不存在的文件
        try {
            Files.readString(Path.of("/non/existent/file.txt"));
        } catch (IOException e) {
            ExceptionHandler.handleConfigException(e, "读取不存在的文件失败");
        }

        // 测试写入到无权限的目录
        try {
            Files.writeString(Path.of("/root/test.txt"), "test content");
        } catch (IOException e) {
            ExceptionHandler.handleConfigException(e, "写入无权限目录失败");
        }

        // 测试读取损坏的文件
        try {
            File tempFile = File.createTempFile("corrupt", ".txt");
            tempFile.deleteOnExit();
            Files.writeString(tempFile.toPath(), "测试内容");
            // 模拟文件损坏
            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                raf.seek(0);
                raf.write(new byte[]{(byte) 0xFF, (byte) 0xFE});
            }
            Files.readString(tempFile.toPath());
        } catch (IOException e) {
            ExceptionHandler.handleConfigException(e, "读取损坏文件失败");
        }
    }
}