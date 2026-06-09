package cn.tealc.ntemaid.ui.taygedo.gacha;

import cn.tealc.ntemaid.model.game.Character;
import cn.tealc.ntemaid.model.game.Weapon;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.LocalGachaDataService;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private ObjectMapper mapper = new ObjectMapper();
    private LocalGachaDataService localGachaDataService = new LocalGachaDataService();



    public GameGachaViewModel() {
        selectedAccount.addListener((obs, old, val) -> {
            if (val != null) loadGachaData();
        });
    }

    public void initialize() {
        loadBaseData();
        refreshAccountList();
    }

    /**
     * 加载角色武器数据
     *
     *
     * @author leck
     * @date 2026/06/09
     */
    private void loadBaseData(){
        File weaponFile = new File("resource/data/weapon.json");
        File characterFile = new File("resource/data/character.json");
        try {
            if (weaponFile.exists()){
                Map<String, Weapon> rawWeaponMap = mapper.readValue(weaponFile, new TypeReference<Map<String, Weapon>>() {});
                weaponMap = new java.util.HashMap<>();
                for (Map.Entry<String, Weapon> entry : rawWeaponMap.entrySet()) {
                    weaponMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
            if (characterFile.exists()){
                Map<String, Character> rawCharacterMap = mapper.readValue(characterFile, new TypeReference<Map<String, Character>>() {});
                characterMap = new java.util.HashMap<>();
                for (Map.Entry<String, Character> entry : rawCharacterMap.entrySet()) {
                    characterMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
        }catch (Exception e){
            LOG.info("加载角色武器数据失败",e);
        }
    }

    public Optional<Weapon> getWeapon(String key){
        if (weaponMap != null)
            return Optional.ofNullable(weaponMap.get(key.toLowerCase()));
        else
            return Optional.empty();
    }

    public Optional<Character> getCharacter(String key){
        if (characterMap != null)
            return Optional.ofNullable(characterMap.get(key.toLowerCase()));
        else
            return Optional.empty();
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
        gachaResult.set(null);

        Thread.ofVirtual().start(() -> {
            try {
                GameGachaResult result = api.getGameGacha(account.getAccessToken());
                localGachaDataService.saveAll(result);


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
