package cn.tealc.ntemaid.ui.system;

import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.AppRuntimeData;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.system.GameServerService;
import cn.tealc.ntemaid.service.taygedo.TaygedoAccountService;
import cn.tealc.ntemaid.service.taygedo.TaygedoRoleService;
import cn.tealc.ntemaid.util.GameClientType;
import cn.tealc.ntemaid.util.ImageCacheManager;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.RoleHome;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 角色面板 ViewModel，负责加载塔吉多角色综合面板数据。
 * <p>数据加载由 {@link NotificationKey#HOME_ROLE_DATA_REFRESH} 通知触发，
 * 该通知在 MainViewModel 完成 token 刷新后发布，确保使用有效令牌请求。
 */
public class RoleBoardByTaygedoViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(RoleBoardByTaygedoViewModel.class);

    /** 头像远程基础 URL */
    private static final String AVATAR_BASE =
            "https://webstatic.tajiduo.com/bbs/yh-game-records-web-source/avatar/square/";

    private final TaygedoRoleService roleService;
    private final TaygedoAccountService accountService;
    private final ImageCacheManager imageCacheManager;
    private final AppRuntimeData appRuntimeData;

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();
    private final ObjectProperty<TaygedoAccount> selectedAccount = new SimpleObjectProperty<>();
    private final ObjectProperty<RoleHome> roleHome = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final ObjectProperty<Image> avatarImage = new SimpleObjectProperty<>();
    private final StringProperty roleDisplay = new SimpleStringProperty("");
    private final ObjectProperty<Image> staminaIcon = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> cityStaminaIcon = new SimpleObjectProperty<>();
    private final BooleanProperty visible = new SimpleBooleanProperty(false);

    /** 默认头像（程序图标），在本地头像缺失时使用 */
    private final Image defaultAvatar;

    /** 监听 token 刷新完成通知的 observer */
    private final NotificationObserver tokenRefreshObserver;

    @Inject
    public RoleBoardByTaygedoViewModel(TaygedoRoleService roleService,
                                        TaygedoAccountService accountService,
                                        ImageCacheManager imageCacheManager,
                                        AppRuntimeData appRuntimeData) {
        this.roleService = roleService;
        this.accountService = accountService;
        this.imageCacheManager = imageCacheManager;
        this.appRuntimeData = appRuntimeData;
        this.defaultAvatar = new Image(FXResourcesLoader.load("image/icon.png"), 40, 40, true, true);
        this.staminaIcon.set(new Image(FXResourcesLoader.load("image/game/stamina.png"), 32, 32, true, true));
        this.cityStaminaIcon.set(new Image(FXResourcesLoader.load("image/game/citystamina.png"), 32, 32, true, true));

        tokenRefreshObserver = (key, payload) -> Platform.runLater(this::onTokenRefreshed);
    }

    /**
     * 初始化：订阅 token 刷新通知。
     * 账号列表加载和角色数据加载延迟到收到 HOME_ROLE_DATA_REFRESH 通知后执行。
     */
    public void initialize() {
        // 异步判断是否显示角色面板：需开启塔吉多 + 国服 + 有账号
        Thread.startVirtualThread(() -> {
            boolean enable = Config.getSetting().isEnableTaygedo();
            boolean isCN = AppInjector.getInstance(GameServerService.class).detectServer() != GameClientType.GLOBAL;
            boolean hasAccount = AppInjector.getInstance(TaygedoAccountService.class).getFirst().isPresent();
            Platform.runLater(() -> visible.set(enable && isCN && hasAccount));
        });

        // 如果 token 已刷新完成（ViewModel 加载时刷新已结束），直接加载数据
        if (appRuntimeData.isTaygedoTokenRefreshed()) {
            onTokenRefreshed();
        } else {
            // 等待 token 刷新完成通知
            NotificationManager.subscribe(NotificationKey.HOME_ROLE_DATA_REFRESH, tokenRefreshObserver);
        }

        selectedAccount.addListener((obs, old, val) -> {
            if (val != null) loadRoleHome();
        });
    }

    /**
     * token 刷新完成后回调：重新加载账号列表（获取最新令牌），首次选中后加载数据
     */
    private void onTokenRefreshed() {
        Thread.startVirtualThread(() -> {
            List<TaygedoAccount> accounts = accountService.getAll();
            Platform.runLater(() -> {
                accountList.setAll(accounts);
                if (!accounts.isEmpty()) {
                    if (selectedAccount.get() == null) {
                        selectedAccount.set(accounts.getFirst());
                    } else {
                        loadRoleHome();
                    }
                }
            });
        });
    }

    /**
     * 加载当前选中账号的角色综合面板数据
     */
    private void loadRoleHome() {
        TaygedoAccount account = selectedAccount.get();
        if (account == null || account.getAccessToken() == null) {
            statusMessage.set("账号未登录，无有效令牌");
            return;
        }
        loading.set(true);
        statusMessage.set("");

        Thread.ofVirtual().start(() -> {
            try {
                RoleHome result = roleService.getRoleHome(account);
                Platform.runLater(() -> {
                    roleHome.set(result);
                    loading.set(false);
                    // 更新角色显示文本（只显示角色名）
                    roleDisplay.set(result.getRolename());
                    // 加载头像
                    loadAvatar(result.getAvatar());
                });
            } catch (TaygedoException e) {
                LOG.error("获取角色面板失败: {}", e.getMessage());
                Platform.runLater(() -> {
                    loading.set(false);
                    statusMessage.set(e.getMessage());
                });
            } catch (Exception e) {
                LOG.error("获取角色面板失败", e);
                Platform.runLater(() -> {
                    loading.set(false);
                    statusMessage.set("获取失败: " + e.getMessage());
                });
            }
        });
    }

    /**
     * 从 ImageCacheManager 加载角色头像。
     * 优先命中本地磁盘缓存（resources/cache/image/{avatar}.png），
     * 未命中则从远程下载；加载失败时使用程序图标。
     */
    private void loadAvatar(String avatarId) {
        if (avatarId == null || avatarId.isBlank()) {
            avatarImage.set(defaultAvatar);
            return;
        }
        Thread.ofVirtual().start(() -> {
            try {
                Image img = imageCacheManager.get(
                        AVATAR_BASE + avatarId + ".PNG", 40, 40, true, true);
                Platform.runLater(() -> avatarImage.set(img));
            } catch (Exception e) {
                LOG.warn("头像加载失败: {}", avatarId, e);
                Platform.runLater(() -> avatarImage.set(defaultAvatar));
            }
        });
    }

    /**
     * 刷新当前账号的角色面板数据
     */
    public void refresh() {
        loadRoleHome();
    }

    // ==================== 属性 getter ====================

    public ObservableList<TaygedoAccount> getAccountList() { return accountList; }

    public ObjectProperty<TaygedoAccount> selectedAccountProperty() { return selectedAccount; }

    public ObjectProperty<RoleHome> roleHomeProperty() { return roleHome; }

    public BooleanProperty loadingProperty() { return loading; }

    public StringProperty statusMessageProperty() { return statusMessage; }

    public ObjectProperty<Image> avatarImageProperty() { return avatarImage; }

    public StringProperty roleDisplayProperty() { return roleDisplay; }

    public ObjectProperty<Image> staminaIconProperty() { return staminaIcon; }

    public ObjectProperty<Image> cityStaminaIconProperty() { return cityStaminaIcon; }

    public BooleanProperty visibleProperty() { return visible; }
}
