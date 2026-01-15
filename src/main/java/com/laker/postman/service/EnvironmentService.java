package com.laker.postman.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.laker.postman.model.Environment;
import com.laker.postman.model.Workspace;
import com.laker.postman.util.VariableUtil;
import com.laker.postman.util.SystemUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 环境变量管理服务，负责环境变量的持久化、加载和处理
 */
@Slf4j
public class EnvironmentService {

    private EnvironmentService() {
        // 工具类不应该被实例化
    }

    private static final Map<String, Environment> environments = Collections.synchronizedMap(new LinkedHashMap<>());
    /**
     * -- GETTER --
     * 获取当前激活的环境
     */
    @Getter
    private static Environment activeEnvironment = null;

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(.+?)}}");

    private static final String DEFAULT_ENV_NAME = "Default Env";

    // 临时变量，仅本次请求有效，优先级高于环境变量
    private static final ThreadLocal<Map<String, String>> temporaryVariables = ThreadLocal.withInitial(ConcurrentHashMap::new);

    // 当前数据文件路径
    private static String currentDataFilePath;

    static {
        loadEnvironments();
    }

    public static void setTemporaryVariable(String key, String value) {
        if (value != null) {
            temporaryVariables.get().put(key, value);
        } else {
            temporaryVariables.get().remove(key);
        }
    }

    public static String getTemporaryVariable(String key) {
        return temporaryVariables.get().get(key);
    }

    public static void clearTemporaryVariables() {
        temporaryVariables.get().clear();
        temporaryVariables.remove();
    }

    /**
     * 获取当前数据文件路径
     */
    public static String getDataFilePath() {
        if (currentDataFilePath != null) {
            return currentDataFilePath;
        }
        // 如果没有设置，返回当前工作区的环境文件路径
        Workspace currentWorkspace = WorkspaceService.getInstance().getCurrentWorkspace();
        return SystemUtil.getEnvPathForWorkspace(currentWorkspace);
    }

    /**
     * 切换环境变量数据文件路径，并重新加载
     */
    public static void setDataFilePath(String path) {
        if (path == null || path.isBlank()) return;
        currentDataFilePath = path;
        loadEnvironmentsFromPath(path);
    }

    /**
     * 从指定路径加载环境变量
     */
    private static void loadEnvironmentsFromPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.info("环境变量文件不存在: {}, 将创建默认环境", filePath);
            createDefaultEnvironments();
            return;
        }

        try {
            environments.clear();
            JSONArray array = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
            for (Object obj : array) {
                Environment env = JSONUtil.toBean((JSONObject) obj, Environment.class);
                environments.put(env.getId(), env);
                if (env.isActive()) {
                    activeEnvironment = env;
                }
            }

            if (activeEnvironment == null && !environments.isEmpty()) {
                // 如果没有激活的环境，激活第一个
                Environment firstEnv = environments.values().iterator().next();
                firstEnv.setActive(true);
                activeEnvironment = firstEnv;
                saveEnvironments();
            }
        } catch (Exception e) {
            log.error("加载环境变量失败: {}", filePath, e);
            createDefaultEnvironments();
        }
    }

    /**
     * 加载所有环境变量
     */
    public static void loadEnvironments() {
        if (currentDataFilePath != null) {
            loadEnvironmentsFromPath(currentDataFilePath);
        } else {
            Workspace currentWorkspace = WorkspaceService.getInstance().getCurrentWorkspace();
            String filePath = SystemUtil.getEnvPathForWorkspace(currentWorkspace);
            loadEnvironmentsFromPath(filePath);
        }
    }

    /**
     * 创建默认环境
     */
    private static void createDefaultEnvironments() {
        environments.clear();

        // Create Default Environment (empty)
        Environment defaultEnv = new Environment("Default Env");
        defaultEnv.setId("env-" + System.currentTimeMillis());
        defaultEnv.setActive(true);
        defaultEnv.setBaseUrl("");

        environments.put(defaultEnv.getId(), defaultEnv);
        activeEnvironment = defaultEnv;

        saveEnvironments();
    }

    /**
     * 保存所有环境变量
     */
    public static void saveEnvironments() {
        try {
            String filePath;
            if (currentDataFilePath != null) {
                filePath = currentDataFilePath;
            } else {
                Workspace currentWorkspace = WorkspaceService.getInstance().getCurrentWorkspace();
                filePath = SystemUtil.getEnvPathForWorkspace(currentWorkspace);
            }

            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            List<Environment> envList = new ArrayList<>(environments.values());
            String jsonStr = JSONUtil.toJsonPrettyStr(envList);

            FileUtil.writeString(jsonStr, file, StandardCharsets.UTF_8);
            log.debug("环境变量已保存到: {}", filePath);
        } catch (Exception e) {
            log.error("保存环境变量失败", e);
        }
    }

    public static void updateWorkspaceBaseUrl(String filePath, String baseUrl) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        List<Environment> envList = readEnvironmentsFromFile(filePath);
        Environment target = envList.stream()
                .filter(Environment::isActive)
                .findFirst()
                .orElse(envList.isEmpty() ? null : envList.get(0));

        if (target == null) {
            target = createDefaultEnvironment(baseUrl);
            envList.add(target);
        } else {
            target.setActive(true);
            target.setBaseUrl(baseUrl == null ? "" : baseUrl.trim());
        }

        writeEnvironmentsToFile(filePath, envList);
    }

    public static String getWorkspaceBaseUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "";
        }
        List<Environment> envList = readEnvironmentsFromFile(filePath);
        if (envList.isEmpty()) {
            return "";
        }
        Environment target = envList.stream()
                .filter(Environment::isActive)
                .findFirst()
                .orElse(envList.get(0));
        String baseUrl = target.getBaseUrl();
        return baseUrl == null ? "" : baseUrl;
    }

    private static Environment createDefaultEnvironment(String baseUrl) {
        Environment environment = new Environment(DEFAULT_ENV_NAME);
        environment.setId("env-" + System.currentTimeMillis());
        environment.setActive(true);
        environment.setBaseUrl(baseUrl == null ? "" : baseUrl.trim());
        return environment;
    }

    private static List<Environment> readEnvironmentsFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            JSONArray array = JSONUtil.readJSONArray(file, StandardCharsets.UTF_8);
            List<Environment> envList = new ArrayList<>();
            for (Object obj : array) {
                Environment env = JSONUtil.toBean((JSONObject) obj, Environment.class);
                envList.add(env);
            }
            return envList;
        } catch (Exception e) {
            log.error("加载环境变量失败: {}", filePath, e);
            return new ArrayList<>();
        }
    }

    private static void writeEnvironmentsToFile(String filePath, List<Environment> envList) {
        try {
            File file = new File(filePath);
            FileUtil.mkParentDirs(file);
            String jsonStr = JSONUtil.toJsonPrettyStr(envList);
            FileUtil.writeString(jsonStr, file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("保存环境变量失败: {}", filePath, e);
        }
    }

    /**
     * 添加或更新环境
     */
    public static void saveEnvironment(Environment environment) {
        environments.put(environment.getId(), environment);
        saveEnvironments();
    }

    /**
     * 删除环境
     */
    public static void deleteEnvironment(String id) {
        Environment env = environments.remove(id);
        if (env != null && env.isActive() && !environments.isEmpty()) {
            // 如果删除的是当前激活的环境，激活第一个环境
            Environment firstEnv = environments.values().iterator().next();
            firstEnv.setActive(true);
            activeEnvironment = firstEnv;
        }
        // if environments is empty after removal, set activeEnvironment to null
        if (environments.isEmpty()) {
            activeEnvironment = null;
        }
        saveEnvironments();
    }

    /**
     * 获取所有环境
     */
    public static List<Environment> getAllEnvironments() {
        return new ArrayList<>(environments.values());
    }

    /**
     * 设置激活的环境
     */
    public static void setActiveEnvironment(String id) {
        if (activeEnvironment != null) {
            activeEnvironment.setActive(false);
        }

        Environment env = environments.get(id);
        if (env != null) {
            env.setActive(true);
            activeEnvironment = env;
            saveEnvironments();
        }
    }

    /**
     * 替换文本中的环境变量占位符
     * 例如: {{baseUrl}}/api/users -> https://api.example.com/api/users
     * 优先级: 临时变量 > 环境变量 > 内置函数
     */
    public static String replaceVariables(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher matcher = VAR_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String value = temporaryVariables.get().get(varName); // 优先查临时变量
            if (value == null && activeEnvironment != null) {
                value = activeEnvironment.getVariable(varName);
            }
            // 检查是否是内置函数
            if (value == null && VariableUtil.isBuiltInFunction(varName)) {
                value = VariableUtil.generateBuiltInFunctionValue(varName);
            }
            // 如果变量不存在，保留原样
            if (value == null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            } else {
                // 使用 Matcher.quoteReplacement 来避免 $ 和 \ 被当作特殊字符处理
                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 根据指定的id顺序重排environments并持久化
     */
    public static void saveEnvironmentOrder(List<String> idOrder) {
        synchronized (environments) {
            LinkedHashMap<String, Environment> newOrder = new LinkedHashMap<>();
            for (String id : idOrder) {
                Environment env = environments.get(id);
                if (env != null) {
                    newOrder.put(id, env);
                }
            }
            // 补充未在idOrder中的环境（防止遗漏）
            for (Map.Entry<String, Environment> entry : environments.entrySet()) {
                if (!newOrder.containsKey(entry.getKey())) {
                    newOrder.put(entry.getKey(), entry.getValue());
                }
            }
            environments.clear();
            environments.putAll(newOrder);
            saveEnvironments();
        }
    }
}