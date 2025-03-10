package com.daicy.devtools.plugin.utils;

/**
 * 配置相关异常类，用于处理配置文件加载、解析等过程中的异常
 */
public class ConfigException extends RuntimeException {
    
    /**
     * 创建一个新的配置异常
     *
     * @param message 异常信息
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * 创建一个新的配置异常
     *
     * @param message 异常信息
     * @param cause   原始异常
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个新的配置异常
     *
     * @param cause 原始异常
     */
    public ConfigException(Throwable cause) {
        super(cause);
    }
}