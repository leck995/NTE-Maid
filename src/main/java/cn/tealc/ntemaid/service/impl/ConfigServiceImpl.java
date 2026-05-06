package cn.tealc.ntemaid.service.impl;

import cn.tealc.ntemaid.dao.ConfigDao;
import cn.tealc.ntemaid.service.ConfigService;
import javafx.util.Pair;

import java.util.*;

public class ConfigServiceImpl implements ConfigService {
    private final ConfigDao configDao = new ConfigDao();

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