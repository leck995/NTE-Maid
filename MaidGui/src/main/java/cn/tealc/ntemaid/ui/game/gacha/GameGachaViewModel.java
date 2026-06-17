package cn.tealc.ntemaid.ui.game.gacha;

import cn.tealc.ntemaid.model.game.Character;
import cn.tealc.ntemaid.model.game.Weapon;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaData;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.repository.GameDataRepository;
import cn.tealc.ntemaid.service.gacha.LocalGachaAnalysisService;
import cn.tealc.ntemaid.service.gacha.LocalGachaDataService;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.ntemaid.util.ImageCacheManager;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.GameGachaResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameGachaViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(GameGachaViewModel.class);

    private final TaygedoAccountService accountService;
    private final TaygedoApi api;
    private final LocalGachaDataService localGachaDataService;
    private final LocalGachaAnalysisService analysisService;
    private final ImageCacheManager imageCacheManager;
    private final GameDataRepository gameDataRepository;

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();
    private final ObjectProperty<TaygedoAccount> selectedAccount = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalGachaData> gachaData = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty avatar = new SimpleStringProperty();
    private final StringProperty roleName = new SimpleStringProperty();
    private final IntegerProperty level = new SimpleIntegerProperty();
    private final StringProperty luckTitle = new SimpleStringProperty();

    private Map<String, Weapon> weaponMap;
    private Map<String, Character> characterMap;
    private final ObjectMapper mapper = new ObjectMapper();


    @Inject
    public GameGachaViewModel(TaygedoAccountService accountService,
                               TaygedoApi api,
                               LocalGachaDataService localGachaDataService,
                               LocalGachaAnalysisService analysisService,
                               ImageCacheManager imageCacheManager,
                              GameDataRepository gameDataRepository) {
        this.accountService = accountService;
        this.api = api;
        this.localGachaDataService = localGachaDataService;
        this.analysisService = analysisService;
        this.imageCacheManager = imageCacheManager;
        this.gameDataRepository = gameDataRepository;
        selectedAccount.addListener((obs, old, val) -> {
            if (val != null) loadGachaData();
        });
    }

    public void init() {
        loadBaseData();
        refreshAccountList();
    }

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

    public Optional<Weapon> getWeapon(String key) {
        return Optional.ofNullable(gameDataRepository.getWeaponMap().get(key.toLowerCase()));
    }

    public Optional<Weapon> getCharacter(String key) {
        return Optional.ofNullable(gameDataRepository.getCharacterMap().get(key.toLowerCase()));
    }

    private void refreshAccountList() {
        List<TaygedoAccount> accounts = accountService.getAll();
        accountList.setAll(accounts);
        if (!accounts.isEmpty() && selectedAccount.get() == null) {
            selectedAccount.set(accounts.get(0));
        }
    }

    private void loadGachaData() {
        TaygedoAccount account = selectedAccount.get();
        if (account == null || account.getAccessToken() == null) {
            statusMessage.set("账号未登录，无有效令牌");
            return;
        }

        loading.set(true);
        statusMessage.set("");
        gachaData.set(null);

        Thread.ofVirtual().start(() -> {
            try {
                GameGachaResult result = api.getGameGacha(account.getAccessToken());
                localGachaDataService.saveAll(result);
                Platform.runLater(() -> {
                    avatar.set(result.getAvatar());
                    roleName.set(result.getRolename());
                    level.set(result.getLev());
                    luckTitle.set(result.getLuckTitle());
                });
                LocalGachaData data = analysisService.analysis(result.getRoleid());
                Platform.runLater(() -> {
                    gachaData.set(data);
                    loading.set(false);
                });
            } catch (TaygedoException e) {
                LOG.error("获取抽卡数据失败: {}", e.getMessage());
                LocalGachaData data = analysisService.analysis(account.getRoleId() != null ? account.getRoleId() : "");
                Platform.runLater(() -> {
                    gachaData.set(data);
                    loading.set(false);
                    statusMessage.set(e.getMessage());
                });
            } catch (Exception e) {
                LOG.error("获取抽卡数据失败", e);
                LocalGachaData data = analysisService.analysis(account.getRoleId() != null ? account.getRoleId() : "");
                Platform.runLater(() -> {
                    gachaData.set(data);
                    loading.set(false);
                    statusMessage.set("获取失败: " + e.getMessage());
                });
            }
        });
    }

    public void onRefresh() {
        loadGachaData();
    }

    // ---- properties ----

    public ObservableList<TaygedoAccount> getAccountList() { return accountList; }
    public ObjectProperty<TaygedoAccount> selectedAccountProperty() { return selectedAccount; }
    public ObjectProperty<LocalGachaData> gachaDataProperty() { return gachaData; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty avatarProperty() { return avatar; }
    public StringProperty roleNameProperty() { return roleName; }
    public IntegerProperty levelProperty() { return level; }
    public StringProperty luckTitleProperty() { return luckTitle; }
    public ImageCacheManager getImageCacheManager() { return imageCacheManager; }
}
