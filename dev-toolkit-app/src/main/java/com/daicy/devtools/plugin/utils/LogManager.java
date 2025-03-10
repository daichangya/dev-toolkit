package com.daicy.devtools.plugin.utils;

import java.io.IOException;
import java.util.logging.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 日志管理器，负责统一管理系统的日志记录功能
 */
public class LogManager {
    private static final Logger rootLogger = Logger.getLogger("");
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;
    private static final String LOG_FORMAT = "[%1$tF %1$tT] [%2$s] [%4$s] %5$s%6$s%n";

    /**
     * 初始化日志管理器
     *
     * @param logFilePath 日志文件路径
     * @throws ConfigException 如果日志配置失败
     */
    public static void init(String logFilePath) {
        try {
            // 配置根日志记录器
            rootLogger.setLevel(Level.ALL);
            
            // 移除默认处理器
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // 创建并配置文件处理器
            Path logPath = Paths.get(logFilePath);
            fileHandler = new FileHandler(logPath.toString(), true);
            fileHandler.setFormatter(createFormatter());
            fileHandler.setLevel(Level.INFO);
            rootLogger.addHandler(fileHandler);

            // 创建并配置控制台处理器
            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(createFormatter());
            consoleHandler.setLevel(Level.ALL);
            rootLogger.addHandler(consoleHandler);

        } catch (IOException e) {
            throw new ConfigException("初始化日志系统失败", e);
        }
    }

    /**
     * 创建日志格式化器
     */
    private static Formatter createFormatter() {
        return new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format(LOG_FORMAT,
                    record.getMillis(),
                    record.getSourceClassName(),
                    record.getSourceMethodName(),
                    record.getLevel().getLocalizedName(),
                    record.getMessage(),
                    record.getThrown() == null ? "" : "\n" + formatException(record.getThrown())
                );
            }
        };
    }

    /**
     * 格式化异常信息
     */
    private static String formatException(Throwable thrown) {
        if (thrown == null) return "";
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : thrown.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append('\n');
        }
        return sb.toString();
    }

    /**
     * 获取指定类的日志记录器
     *
     * @param clazz 类对象
     * @return 日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * 关闭日志处理器
     */
    public static void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
        if (consoleHandler != null) {
            consoleHandler.close();
        }
    }
}