package com.laker.postman.performance.stats;

public class PerformanceTrendSnapshot {
    private final int activeUsers;
    private final double avgResponseTime;
    private final double qps;
    private final double errorPercent;
    private final int sampleCount;

    public PerformanceTrendSnapshot(int activeUsers, double avgResponseTime, double qps, double errorPercent, int sampleCount) {
        this.activeUsers = activeUsers;
        this.avgResponseTime = avgResponseTime;
        this.qps = qps;
        this.errorPercent = errorPercent;
        this.sampleCount = sampleCount;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public double getQps() {
        return qps;
    }

    public double getErrorPercent() {
        return errorPercent;
    }

    public int getSampleCount() {
        return sampleCount;
    }
}
