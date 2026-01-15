package com.laker.postman.performance.stats;

import com.laker.postman.panel.performance.model.RequestResult;

import java.util.List;
import java.util.Map;

public class PerformanceReportSnapshot {
    private final Map<String, List<Long>> apiCostMap;
    private final Map<String, Integer> apiSuccessMap;
    private final Map<String, Integer> apiFailMap;
    private final List<Long> requestStartTimes;
    private final List<RequestResult> requestResults;

    public PerformanceReportSnapshot(Map<String, List<Long>> apiCostMap,
                                    Map<String, Integer> apiSuccessMap,
                                    Map<String, Integer> apiFailMap,
                                    List<Long> requestStartTimes,
                                    List<RequestResult> requestResults) {
        this.apiCostMap = apiCostMap;
        this.apiSuccessMap = apiSuccessMap;
        this.apiFailMap = apiFailMap;
        this.requestStartTimes = requestStartTimes;
        this.requestResults = requestResults;
    }

    public Map<String, List<Long>> getApiCostMap() {
        return apiCostMap;
    }

    public Map<String, Integer> getApiSuccessMap() {
        return apiSuccessMap;
    }

    public Map<String, Integer> getApiFailMap() {
        return apiFailMap;
    }

    public List<Long> getRequestStartTimes() {
        return requestStartTimes;
    }

    public List<RequestResult> getRequestResults() {
        return requestResults;
    }
}
