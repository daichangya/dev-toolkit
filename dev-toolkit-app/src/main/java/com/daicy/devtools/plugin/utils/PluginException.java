package com.daicy.devtools.plugin.utils;

/**
 * 插件相关异常类，用于处理插件加载、运行等过程中的异常
 */
public class PluginException extends RuntimeException {
    
    /**
     * 创建一个新的插件异常
     *
     * @param message 异常信息
     */
    public PluginException(String message) {
        super(message);
    }

    /**
     * 创建一个新的插件异常
     *
     * @param message 异常信息
     * @param cause   原始异常
     */
    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个新的插件异常
     *
     * @param cause 原始异常
     */
    public PluginException(Throwable cause) {
        super(cause);
    }
}