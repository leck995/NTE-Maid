package cn.tealc.ntemaid.ui.taygedo.signin;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.ntemaid.service.TaygedoSignInService;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.SigninReward;
import cn.tealc.taygedo.model.SigninState;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TaygedoSignInViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoSignInViewModel.class);

    private final TaygedoSignInService signInService = new TaygedoSignInService();
    private final TaygedoAccountService accountService = new TaygedoAccountService();

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();
    private final ObservableList<SigninReward> rewardList = FXCollections.observableArrayList();
    private final ObjectProperty<TaygedoAccount> selectedAccount = new SimpleObjectProperty<>();
    private final IntegerProperty signedDays = new SimpleIntegerProperty(0);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty autoSign;

    public TaygedoSignInViewModel() {
        autoSign = Config.setting.taygedoAutoSignProperty();
    }

    public void initialize() {
        refreshAccountList();
        selectedAccount.addListener((obs, old, val) -> {
            if (val != null) {
                loadSignInData();
            }
        });
    }

    private void refreshAccountList() {
        List<TaygedoAccount> accounts = accountService.getAll();
        accountList.setAll(accounts);
        if (!accounts.isEmpty() && selectedAccount.get() == null) {
            selectedAccount.set(accounts.getFirst());
        }
    }

    private void loadSignInData() {
        TaygedoAccount account = selectedAccount.get();
        if (account == null) return;

        loading.set(true);
        Thread.ofVirtual().start(() -> {
            try {
                List<SigninReward> rewards = signInService.getSigninRewards(account);
                SigninState state = signInService.getSigninState(account);

                Platform.runLater(() -> {
                    rewardList.setAll(rewards);
                    if (state != null) {
                        signedDays.set(state.getDays());
                    }
                    setStatus(String.format("本月共 %d 天奖励，已签到 %d 天",
                            rewards.size(), state != null ? state.getDays() : 0));
                });
            } catch (Exception e) {
                LOG.error("加载签到数据失败", e);
                Platform.runLater(() -> setStatus("加载失败: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    public void signIn() {
        TaygedoAccount account = selectedAccount.get();
        if (account == null) {
            setStatus("请先选择账号");
            return;
        }

        loading.set(true);
        setStatus("正在签到...");
        Thread.ofVirtual().start(() -> {
            try {
                signInService.gameSignin(account);
                Platform.runLater(() -> {
                    setStatus("签到成功！");
                    loadSignInData();
                });
            } catch (TaygedoException e) {
                Platform.runLater(() -> setStatus("签到失败: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    public void signInAll() {
        List<TaygedoAccount> accounts = List.copyOf(accountList);
        if (accounts.isEmpty()) {
            setStatus("没有可签到的账号");
            return;
        }

        loading.set(true);
        Thread.ofVirtual().start(() -> {
            int success = 0;
            int fail = 0;
            for (int i = 0; i < accounts.size(); i++) {
                TaygedoAccount account = accounts.get(i);
                try {
                    signInService.gameSignin(account);
                    success++;
                    int finalI = i + 1;
                    Platform.runLater(() -> setStatus(
                            String.format("全部签到: [%d/%d] %s 成功", finalI, accounts.size(), account.getPhone())));
                } catch (TaygedoException e) {
                    fail++;
                    int finalI = i + 1;
                    Platform.runLater(() -> setStatus(
                            String.format("全部签到: [%d/%d] %s 失败: %s", finalI, accounts.size(), account.getPhone(), e.getMessage())));
                }

                if (i < accounts.size() - 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            int finalSuccess = success;
            int finalFail = fail;
            Platform.runLater(() -> {
                setStatus(String.format("全部签到完成: 成功 %d, 失败 %d", finalSuccess, finalFail));
                loadSignInData();
                loading.set(false);
            });
        });
    }

    private void setStatus(String msg) {
        Platform.runLater(() -> statusMessage.set(msg));
    }

    // ==================== Property Getters ====================

    public ObservableList<TaygedoAccount> getAccountList() { return accountList; }
    public ObservableList<SigninReward> getRewardList() { return rewardList; }
    public ObjectProperty<TaygedoAccount> selectedAccountProperty() { return selectedAccount; }
    public IntegerProperty signedDaysProperty() { return signedDays; }
    public BooleanProperty loadingProperty() { return loading; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty autoSignProperty() { return autoSign; }
}
