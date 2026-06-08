package cn.tealc.ntemaid.service;

import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConfigService {
    /**
     * 获取配置，若不存在则返回 Optional.empty()
     */
    Optional<String> getConfig(String key);

    /**
     * 获取整数配置，若不存在或转换失败则返回 Optional.empty()
     */
    Optional<Integer> getIntConfig(String key);

    /**
     * 获取布尔配置，若不存在则返回 Optional.empty()
     */
    Optional<Boolean> getBooleanConfig(String key);

    Optional<Pair<String,String>> getPairConfig(String key);

    List<String> getListConfig(String key);

    void setConfig(String key, Object value);

    void setConfig(String key, String... values);
    
    void setConfigs(Map<String, String> configs);

    void removeConfig(String key);
}