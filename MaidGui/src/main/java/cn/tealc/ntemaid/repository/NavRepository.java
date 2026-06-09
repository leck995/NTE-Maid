package cn.tealc.ntemaid.repository;

import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.model.system.nav.NavData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Singleton
public class NavRepository {
    private static final Logger LOG = LoggerFactory.getLogger(NavRepository.class);
    private static final String NAV_JSON_PATH = "/cn/tealc/ntemaid/data/nav.json";
    private final ObjectMapper mapper;

    @Inject
    public NavRepository(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<NavData> load() {
        InputStream inputStream = FXResourcesLoader.loadStream(NAV_JSON_PATH);
        try {
            return mapper.readValue(inputStream, new TypeReference<List<NavData>>() {});
        } catch (IOException e) {
            LOG.error("加载导航数据失败", e);
            return Collections.emptyList();
        }
    }
}
