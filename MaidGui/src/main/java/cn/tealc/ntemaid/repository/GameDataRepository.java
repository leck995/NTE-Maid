package cn.tealc.ntemaid.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 游戏静态数据仓库，负责加载 {@code resources/data/} 下的元数据文件，
 * 对外提供稀有度、中文名称、UP 判定等查询。
 */
@Singleton
public class GameDataRepository {
    private static final Logger LOG = LoggerFactory.getLogger(GameDataRepository.class);

    private static final Set<String> STANDARD_ROLE_5 = Set.of(
            "1055", "1054", "1039", "1025", "1023", "1003");
    private static final Set<String> STANDARD_FORK_5 = Set.of(
            "fork_butterfly", "fork_blackBook", "fork_mofeikesi",
            "fork_jingmotingyuan", "fork_wushoutieyu", "fork_bitGame", "fork_rishi",
            "fork_nestBird", "fork_arachne", "fork_whale");

    /** itemId → rarity */
    private final Map<String, Integer> rarityMap = new HashMap<>();
    /** itemId → 中文名称 */
    private final Map<String, String> nameMap = new HashMap<>();

    private final ObjectMapper mapper;

    @Inject
    public GameDataRepository(ObjectMapper mapper) {
        this.mapper = mapper;
        loadFile("resources/data/character.json");
        loadFile("resources/data/weapon.json");
    }

    private void loadFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.warn("数据文件不存在: {}", filePath);
            return;
        }
        try {
            Map<String, Map<String, String>> raw = mapper.readValue(file,
                    new TypeReference<Map<String, Map<String, String>>>() {});
            for (Map.Entry<String, Map<String, String>> entry : raw.entrySet()) {
                Map<String, String> value = entry.getValue();
                if (value.containsKey("rarity")) {
                    rarityMap.put(entry.getKey(), Integer.parseInt(value.get("rarity")));
                }
                if (value.containsKey("zh")) {
                    nameMap.put(entry.getKey(), value.get("zh"));
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOG.error("加载数据文件失败: {}", filePath, e);
        }
    }

    /** 查询稀有度，无数据时默认返回 3 */
    public int getRarity(String itemId) {
        if (itemId == null) return 3;
        return rarityMap.getOrDefault(itemId, 3);
    }

    /** 查询中文名称，无数据时返回 null */
    public String getChineseName(String itemId) {
        if (itemId == null) return null;
        return nameMap.get(itemId);
    }

    /** 判断是否为 UP（限定）物品：不在常驻 5★ 列表中的即为 UP */
    public boolean isUp(String itemId, boolean isFork) {
        if (itemId == null) return false;
        if (isFork) {
            return !STANDARD_FORK_5.contains(itemId.toLowerCase());
        }
        return !STANDARD_ROLE_5.contains(itemId);
    }
}
