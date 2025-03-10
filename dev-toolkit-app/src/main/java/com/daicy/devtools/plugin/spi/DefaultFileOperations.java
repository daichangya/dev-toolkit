package com.daicy.devtools.plugin.spi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 默认的文件操作实现
 * 提供基本的文件读写功能
 */
public class DefaultFileOperations implements FileOperations {
    private static DefaultFileOperations instance;

    private DefaultFileOperations() {}

    public static DefaultFileOperations getInstance() {
        if (instance == null) {
            instance = new DefaultFileOperations();
        }
        return instance;
    }

    @Override
    public String readFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Override
    public void writeToFile(String content, File file) throws IOException {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}