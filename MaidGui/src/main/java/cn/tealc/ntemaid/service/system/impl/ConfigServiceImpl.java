package cn.tealc.ntemaid.service.system.impl;

import cn.tealc.ntemaid.dao.ConfigDao;
import cn.tealc.ntemaid.service.system.ConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConfigServiceImpl implements ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigServiceImpl.class);
    private final ConfigDao configDao;
    private final ObjectMapper mapper;

    @Inject
    public ConfigServiceImpl(ConfigDao configDao, ObjectMapper mapper) {
        this.configDao = configDao;
        this.mapper = mapper;
    }

    @Override
    public Optional<String> getConfig(String key) {
        return configDao.getValue(key);
    }

    @Override
    public Optional<Integer> getIntConfig(String key) {
        return configDao.getValue(key).map(s -> {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return null; // map 允许返回 null，最终会变成 Optional.empty()
            }
        });
    }

    @Override
    public Optional<Boolean> getBooleanConfig(String key) {
        // Boolean.parseBoolean 不会抛异常，但会将非 "true" 的字符串都转为 false
        // 如果需要严格判断，可以手动处理
        return configDao.getValue(key).map(s -> {
            if ("true".equalsIgnoreCase(s)) return true;
            if ("false".equalsIgnoreCase(s)) return false;
            return null;
        });
    }

    @Override
    public Optional<Pair<String, String>> getPairConfig(String key) {
        return configDao.getValue(key).map(s -> {
            String[] split = s.split(";");
            if (split.length == 2)
                return new Pair<>(split[0], split[1]);
            else
                return null;
        });
    }

    @Override
    public List<String> getListConfig(String key) {
        return configDao.getValue(key).map(s -> {
            String[] split = s.split(";");
            return new ArrayList<>(Arrays.asList(split));
        }).orElse(new ArrayList<>());
    }

    /**
     * 读取 JSON 并反序列化为泛型类型（如 List<Item>）
     */
    public <T> Optional<T> getObjectConfig(String key, TypeReference<T> typeRef) {
        return getConfig(key).map(json -> {
            try {
                return mapper.readValue(json, typeRef);
            } catch (JsonProcessingException e) {
                log.error("反序列化配置失败, key: {}", key, e);
                return null;
            }
        });
    }

    /**
     * 将对象序列化为 JSON 存储
     */
    public <T> void setObject(String key, T value) {
        try {
            String json = mapper.writeValueAsString(value);
            log.info("保存配置对象, key: {}, type: {}", key, value.getClass().getSimpleName());
            setConfig(key, json);
        } catch (JsonProcessingException e) {
            log.error("序列化对象失败, key: {}", key, e);
        }
    }

    @Override
    public void setConfig(String key, Object value) {
        if (key != null && value != null) {
            configDao.saveOrUpdate(key, String.valueOf(value));
        }
    }

    @Override
    public void setConfig(String key, String... values) {
        if (key != null && values != null) {
            String value = String.join(";", values);
            configDao.saveOrUpdate(key, value);
        }
    }

    @Override
    public void setConfigs(Map<String, String> configs) {
        if (configs != null && !configs.isEmpty()) {
            configDao.saveAll(configs);
        }
    }

    @Override
    public void removeConfig(String key) {
        configDao.delete(key);
    }
}