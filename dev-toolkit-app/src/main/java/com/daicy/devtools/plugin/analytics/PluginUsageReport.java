package com.daicy.devtools.plugin.analytics;

import java.time.LocalDateTime;

/**
 * 插件使用报告
 * 用于展示插件的使用统计信息
 */
public record PluginUsageReport(
    String pluginName,
    long usageCount,
    long totalUsageMinutes,
    LocalDateTime lastUsed
) {}