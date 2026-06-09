package cn.tealc.ntemaid.ui.taygedo.signin;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.ntemaid.service.TaygedoSignInService;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.SigninReward;
import cn.tealc.taygedo.model.SigninState;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TaygedoSignInViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoSignInViewModel.class);

    private final TaygedoSignInService signInService;
    private final TaygedoAccountService accountService;

    private final ObservableList<TaygedoAccount> accountList = FXCollections.observableArrayList();
    private final ObservableList<SigninReward> rewardList = FXCollections.observableArrayList();
    private final ObjectProperty<TaygedoAccount> selectedAccount = new SimpleObjectProperty<>();
    private final IntegerProperty signedDays = new SimpleIntegerProperty(0);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty autoSign;

    private final Command signInCommand;
    private final Command signInAllCommand;

    @Inject
    public TaygedoSignInViewModel(TaygedoSignInService signInService,
                                   TaygedoAccountService accountService) {
        this.signInService = signInService;
        this.accountService = accountService;
        autoSign = Config.getSetting().taygedoAutoSignProperty();

        signInCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                doSignIn();
            }
        }, selectedAccount.isNotNull(), true);

        signInAllCommand = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                doSignInAll();
            }
        }, selectedAccount.isNotNull() ,true);
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
                Platform.runLater(() -> setStatus("签到失败: " + e.getMessage()));
            }
        });
    }

    private void doSignIn() throws TaygedoException {
        TaygedoAccount account = selectedAccount.get();
        setStatus("正在签到...");
        try {
            signInService.gameSignin(account);
            Platform.runLater(() -> {
                setStatus("签到成功！");
                accountService.refreshLastSignTime(account);
                loadSignInData();
            });
        }catch (Exception e) {
                LOG.error("加载签到数据失败", e);
                Platform.runLater(() -> setStatus("签到失败: " + e.getMessage()));
            }

    }

    private void doSignInAll() throws Exception {
        List<TaygedoAccount> accounts = List.copyOf(accountList);
        if (accounts.isEmpty()) {
            Platform.runLater(() -> setStatus("没有可签到的账号"));
            return;
        }

        int total = accounts.size();
        int success = 0;
        int fail = 0;
        for (int i = 0; i < total; i++) {
            TaygedoAccount account = accounts.get(i);
            try {
                signInService.gameSignin(account);
                accountService.refreshLastSignTime(account);
                success++;
                int finalI = i + 1;
                Platform.runLater(() -> setStatus(
                        String.format("全部签到: [%d/%d] %s 成功", finalI, total, account.getPhone())));
            } catch (TaygedoException e) {
                fail++;
                int finalI = i + 1;
                Platform.runLater(() -> setStatus(
                        String.format("全部签到: [%d/%d] %s 失败: %s", finalI, total, account.getPhone(), e.getMessage())));
            }
            if (i < total - 1) {
                Thread.sleep(1000);
            }
        }
        int finalSuccess = success;
        int finalFail = fail;
        Platform.runLater(() -> {
            setStatus(String.format("全部签到完成: 成功 %d, 失败 %d", finalSuccess, finalFail));
            loadSignInData();
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
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty autoSignProperty() { return autoSign; }

    public Command getSignInCommand() { return signInCommand; }
    public Command getSignInAllCommand() { return signInAllCommand; }
}
