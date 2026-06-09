package cn.tealc.ntemaid.ui.taygedo.gacha;

import cn.tealc.ntemaid.model.game.Character;
import cn.tealc.ntemaid.model.game.Weapon;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaData;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaType;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.LocalGachaDataService;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.GameGachaPool;
import cn.tealc.taygedo.model.GameGachaResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 塔吉多抽卡分析页 ViewModel
 * <p>
 * 负责加载账号列表、获取抽卡数据、将新数据持久化到本地数据库，
 * 并用数据库中的全量数据重新计算卡池统计信息（稀有总次数、抽卡总数、平均出率）。
 *
 * @author leck
 * @date 2026/06/09
 */
public class GameGachaViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(GameGachaViewModel.class);

    private final TaygedoAccountService accountService = new TaygedoAccountService();
    private final TaygedoApi api = new TaygedoApi();

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();
    private final ObjectProperty<TaygedoAccount> selectedAccount = new SimpleObjectProperty<>();
    private final ObjectProperty<GameGachaResult> gachaResult = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private Map<String, Weapon> weaponMap;
    private Map<String, Character> characterMap;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LocalGachaDataService localGachaDataService = new LocalGachaDataService();

    /** 卡池 tab 名称到枚举的映射 */
    private static final Map<String, LocalGachaType> TAB_TYPE_MAP = Map.of(
            "限定卡池", LocalGachaType.UP_ROLE_POOL,
            "常驻卡池", LocalGachaType.DEFAULT_ROLE_POOL,
            "弧盘池", LocalGachaType.WEAPON_POOL
    );

    public GameGachaViewModel() {
        selectedAccount.addListener((obs, old, val) -> {
            if (val != null) loadGachaData();
        });
    }

    /** 初始化：加载本地基础数据并刷新账号列表 */
    public void initialize() {
        loadBaseData();
        refreshAccountList();
    }

    /** 加载武器和角色的本地 JSON 数据，key 统一转小写 */
    private void loadBaseData() {
        File weaponFile = new File("resource/data/weapon.json");
        File characterFile = new File("resource/data/character.json");
        try {
            if (weaponFile.exists()) {
                Map<String, Weapon> rawWeaponMap = mapper.readValue(weaponFile, new TypeReference<Map<String, Weapon>>() {});
                weaponMap = new java.util.HashMap<>();
                for (Map.Entry<String, Weapon> entry : rawWeaponMap.entrySet()) {
                    weaponMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
            if (characterFile.exists()) {
                Map<String, Character> rawCharacterMap = mapper.readValue(characterFile, new TypeReference<Map<String, Character>>() {});
                characterMap = new java.util.HashMap<>();
                for (Map.Entry<String, Character> entry : rawCharacterMap.entrySet()) {
                    characterMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
        } catch (Exception e) {
            LOG.info("加载角色武器数据失败", e);
        }
    }

    /** 忽略大小写查询武器 */
    public Optional<Weapon> getWeapon(String key) {
        if (weaponMap != null)
            return Optional.ofNullable(weaponMap.get(key.toLowerCase()));
        return Optional.empty();
    }

    /** 忽略大小写查询角色 */
    public Optional<Character> getCharacter(String key) {
        if (characterMap != null)
            return Optional.ofNullable(characterMap.get(key.toLowerCase()));
        return Optional.empty();
    }

    /** 刷新账号下拉列表，无选中时自动选第一个 */
    private void refreshAccountList() {
        List<TaygedoAccount> accounts = accountService.getAll();
        accountList.setAll(accounts);
        if (!accounts.isEmpty() && selectedAccount.get() == null) {
            selectedAccount.set(accounts.get(0));
        }
    }

    /** 根据选中账号异步加载抽卡数据 */
    private void loadGachaData() {
        TaygedoAccount account = selectedAccount.get();
        if (account == null || account.getAccessToken() == null) {
            statusMessage.set("账号未登录，无有效令牌");
            return;
        }

        loading.set(true);
        statusMessage.set("");
        gachaResult.set(null);

        Thread.ofVirtual().start(() -> {
            try {
                GameGachaResult result = api.getGameGacha(account.getAccessToken());
                addAndUpdateLocalData(result);
                Platform.runLater(() -> {
                    gachaResult.set(result);
                    loading.set(false);
                });
            } catch (TaygedoException e) {
                LOG.error("获取抽卡数据失败: {}", e.getMessage());
                Platform.runLater(() -> {
                    loading.set(false);
                    statusMessage.set(e.getMessage());
                });
            } catch (Exception e) {
                LOG.error("获取抽卡数据失败", e);
                Platform.runLater(() -> {
                    loading.set(false);
                    statusMessage.set("获取失败: " + e.getMessage());
                });
            }
        });
    }

    /**
     * 将 API 返回的新数据持久化到数据库，再用数据库全量数据覆盖卡池详情并重新计算统计。
     * <p>
     * 步骤：
     * <ol>
     *     <li>按卡池类型分别写入增量数据</li>
     *     <li>从 DB 取出该角色该卡池的全量数据，替换 details</li>
     *     <li>重新计算稀有总次数、抽卡次数、平均出率</li>
     * </ol>
     */
    private void addAndUpdateLocalData(GameGachaResult result) {
        localGachaDataService.saveAll(result);

        for (GameGachaPool pool : result.getGachaDetails()) {
            LocalGachaType type = LocalGachaType.fromName(pool.getTab());
            if (type == LocalGachaType.UNKNOWN)
                continue;
            List<LocalGachaData> gachaDataList = localGachaDataService
                    .getAfterTimeDescByRoleIdAndPoolType(result.getRoleid(), type, 0);
            pool.setDetails(new ArrayList<>(gachaDataList));

            int sum = gachaDataList.stream().mapToInt(LocalGachaData::getRareCount).sum();
            int count = gachaDataList.size();
            pool.setDrawCount(sum);
            pool.setRareCount(count);
            if (count > 0) {
                pool.setAverage(String.format("%.2f", (float) sum / count));
            }
        }
    }

    /** 刷新按钮事件 */
    public void onRefresh() {
        loadGachaData();
    }

    // ---- properties ----

    public ObservableList<TaygedoAccount> getAccountList() { return accountList; }
    public ObjectProperty<TaygedoAccount> selectedAccountProperty() { return selectedAccount; }
    public ObjectProperty<GameGachaResult> gachaResultProperty() { return gachaResult; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty loadingProperty() { return loading; }
}
