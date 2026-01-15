package com.laker.postman.performance.stats;

import com.laker.postman.panel.performance.model.RequestResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceStatistics {
    private final List<Long> requestStartTimes = Collections.synchronizedList(new ArrayList<>());
    private final List<RequestResult> requestResults = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<Long>> apiCostMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> apiSuccessMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> apiFailMap = new ConcurrentHashMap<>();
    private final Object statsLock = new Object();

    public void reset() {
        synchronized (statsLock) {
            requestStartTimes.clear();
            requestResults.clear();
            apiCostMap.clear();
            apiSuccessMap.clear();
            apiFailMap.clear();
        }
    }

    public void recordStart(long startTime) {
        requestStartTimes.add(startTime);
    }

    public void recordResult(String apiName, boolean success, long cost, long endTime) {
        synchronized (statsLock) {
            requestResults.add(new RequestResult(endTime, success, cost));
            apiCostMap.computeIfAbsent(apiName, k -> Collections.synchronizedList(new ArrayList<>())).add(cost);
            if (success) {
                apiSuccessMap.merge(apiName, 1, Integer::sum);
            } else {
                apiFailMap.merge(apiName, 1, Integer::sum);
            }
        }
    }

    public int getResultCount() {
        synchronized (statsLock) {
            return requestResults.size();
        }
    }

    public PerformanceReportSnapshot snapshotForReport() {
        synchronized (statsLock) {
            List<Long> startTimesCopy = new ArrayList<>(requestStartTimes);
            List<RequestResult> resultsCopy = new ArrayList<>(requestResults);
            Map<String, List<Long>> apiCostMapCopy = new HashMap<>();
            for (Map.Entry<String, List<Long>> entry : apiCostMap.entrySet()) {
                apiCostMapCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            Map<String, Integer> apiSuccessMapCopy = new HashMap<>(apiSuccessMap);
            Map<String, Integer> apiFailMapCopy = new HashMap<>(apiFailMap);
            return new PerformanceReportSnapshot(apiCostMapCopy, apiSuccessMapCopy, apiFailMapCopy, startTimesCopy, resultsCopy);
        }
    }

    public PerformanceTrendSnapshot snapshotForTrend(long windowStart, long windowEnd, int activeUsers, int samplingIntervalSeconds) {
        int totalReq = 0;
        int errorReq = 0;
        long totalRespTime = 0;
        long actualMinTime = Long.MAX_VALUE;
        long actualMaxTime = 0;
        synchronized (statsLock) {
            for (RequestResult result : requestResults) {
                if (result.endTime >= windowStart && result.endTime <= windowEnd) {
                    totalReq++;
                    if (!result.success) {
                        errorReq++;
                    }
                    totalRespTime += result.responseTime;
                    actualMinTime = Math.min(actualMinTime, result.endTime);
                    actualMaxTime = Math.max(actualMaxTime, result.endTime);
                }
            }
        }

        double avgRespTime = totalReq > 0
                ? BigDecimal.valueOf((double) totalRespTime / totalReq)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue()
                : 0;

        double qps = 0;
        if (totalReq > 0 && actualMaxTime > actualMinTime) {
            long actualSpanMs = actualMaxTime - actualMinTime;
            qps = totalReq * 1000.0 / actualSpanMs;
        } else if (totalReq > 0) {
            qps = totalReq / (double) samplingIntervalSeconds;
        }

        double errorPercent = totalReq > 0 ? (double) errorReq / totalReq * 100 : 0;
        return new PerformanceTrendSnapshot(activeUsers, avgRespTime, qps, errorPercent, totalReq);
    }
}
