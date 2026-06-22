package cn.tealc.ntemaid.repository;

import cn.tealc.ntemaid.model.game.Weapon;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaType;
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
            "fork_butterfly","fork_blackbook", "fork_mofeikesi",
            "fork_jingmotingyuan", "fork_wushoutieyu", "fork_bitgame", "fork_rishi",
            "fork_nestbird", "fork_arachne", "fork_whale");
    private Map<String, Weapon> characterMap = new HashMap<>();
    private Map<String, Weapon> weaponMap = new HashMap<>();
    private final ObjectMapper mapper;

    @Inject
    public GameDataRepository(ObjectMapper mapper) {
        this.mapper = mapper;
        loadCharacter("resources/data/character.json");
        loadWeapon("resources/data/weapon.json");
    }

    private void loadWeapon(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.warn("数据文件不存在: {}", filePath);
            return;
        }
        try {
            weaponMap = mapper.readValue(file, new TypeReference<Map<String, Weapon>>() {});
        } catch (IOException | NumberFormatException e) {
            LOG.error("加载数据文件失败: {}", filePath, e);
        }
    }

    private void loadCharacter(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.warn("数据文件不存在: {}", filePath);
            return;
        }
        try {
            characterMap = mapper.readValue(file, new TypeReference<Map<String, Weapon>>() {});
        } catch (IOException | NumberFormatException e) {
            LOG.error("加载数据文件失败: {}", filePath, e);
        }
    }


    public Weapon getWeapon(String itemId) {
        return weaponMap.getOrDefault(itemId.toLowerCase(),null);
    }
    public Weapon getCharacter(String itemId) {
        return characterMap.getOrDefault(itemId.toLowerCase(),null);
    }

    public Weapon getCharacterOrWeapon(String itemId) {
        String key = itemId.toLowerCase();
        if (characterMap.containsKey(key)){
            return characterMap.get(key);
        }else {
            if (weaponMap.containsKey(key)){
                return weaponMap.get(key);
            }
        }
        return null;
    }


    /** 判断是否为 UP（限定）物品：不在常驻 5★ 列表中的即为 UP */
    public boolean isUp(String itemId, boolean isFork) {
        if (itemId == null) return false;
        if (isFork) {
            return !STANDARD_FORK_5.contains(itemId.toLowerCase());
        }
        return !STANDARD_ROLE_5.contains(itemId);
    }

    public Map<String, Weapon> getWeaponMap() {
        return weaponMap;
    }

    public void setWeaponMap(Map<String, Weapon> weaponMap) {
        this.weaponMap = weaponMap;
    }

    public Map<String, Weapon> getCharacterMap() {
        return characterMap;
    }

    public void setCharacterMap(Map<String, Weapon> characterMap) {
        this.characterMap = characterMap;
    }
}
