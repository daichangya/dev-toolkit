package com.daicy.devtools.plugin.spi;

import java.io.File;
import java.io.IOException;

/**
 * 文件操作接口
 * 定义了插件系统中统一的文件操作方法
 */
public interface FileOperations {
    /**
     * 从文件中读取内容
     * @param file 要读取的文件
     * @return 文件内容
     * @throws IOException 如果读取过程中发生IO错误
     */
    String readFromFile(File file) throws IOException;

    /**
     * 将内容写入文件
     * @param content 要写入的内容
     * @param file 目标文件
     * @throws IOException 如果写入过程中发生IO错误
     */
    void writeToFile(String content, File file) throws IOException;

    /**
     * 获取文件扩展名
     * @param file 文件
     * @return 文件扩展名
     */
    default String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }
}