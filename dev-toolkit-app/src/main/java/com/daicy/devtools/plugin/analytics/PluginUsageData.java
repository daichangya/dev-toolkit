package com.daicy.devtools.plugin.analytics;

import java.time.Duration;
import java.time.LocalDateTime;

public class PluginUsageData {
    private String pluginName;
    private long usageCount;
    private Duration totalUsageDuration;
    private LocalDateTime lastUsed;

    public PluginUsageData() {
        this.totalUsageDuration = Duration.ZERO;
    }

    public PluginUsageData(String pluginName) {
        this();
        this.pluginName = pluginName;
        this.usageCount = 0;
        this.lastUsed = LocalDateTime.now();
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public Duration getTotalUsageDuration() {
        return totalUsageDuration;
    }

    public void setTotalUsageDuration(Duration totalUsageDuration) {
        this.totalUsageDuration = totalUsageDuration;
    }

    public void addUsageDuration(Duration duration) {
        this.totalUsageDuration = this.totalUsageDuration.plus(duration);
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
}