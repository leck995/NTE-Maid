package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.nav.NavData;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.repository.NavRepository;
import cn.tealc.ntemaid.service.AsyncRunner;
import cn.tealc.ntemaid.service.TaygedoAccountService;
import cn.tealc.ntemaid.service.TaygedoLoginService;
import cn.tealc.ntemaid.service.TaygedoSignInService;
import cn.tealc.ntemaid.thread.system.update.CheckAppVersionTask;
import cn.tealc.ntemaid.thread.system.update.DeleteOldAppVersionTask;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.SigninState;
import cn.tealc.teafx.utils.ResponseBody;
import cn.tealc.teafx.utils.message.MessageInfo;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:
 * @author: Leck
 * @create: 2024-07-03 18:59
 */
public class MainViewModel implements ViewModel, SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(MainViewModel.class);

    private final TaygedoAccountService accountService;
    private final TaygedoLoginService loginService;
    private final TaygedoSignInService signInService;
    private final NavRepository navRepo;
    private final AsyncRunner asyncRunner;
    private final AtomicBoolean started = new AtomicBoolean(false);

    @Inject
    public MainViewModel(TaygedoAccountService accountService,
                         TaygedoLoginService loginService,
                         TaygedoSignInService signInService,
                         NavRepository navRepo,
                         AsyncRunner asyncRunner) {
        this.accountService = accountService;
        this.loginService = loginService;
        this.signInService = signInService;
        this.navRepo = navRepo;
        this.asyncRunner = asyncRunner;
    }

    @Override
    public void onViewAdded() {
        if (started.compareAndSet(false, true)) {
            startup();
        }
    }

    @Override
    public void onViewRemoved() {}

    private void startup() {
        checkVersionAndClean();
        initMusicClient();
        startTaygedoTask();
    }

    private static void initMusicClient() {
        MusicPlayerClient.getInstance().init();
    }

    public List<NavData> getNavList() {
        return navRepo.load();
    }

    public void startTaygedoTask() {
        asyncRunner.runBackground(() -> {
            taygedoRefrshToken();
            autoTaygedoSign();
        });
    }

    private void taygedoRefrshToken() {
        List<TaygedoAccount> accounts = accountService.getAll();
        for (TaygedoAccount account : accounts) {
            try {
                loginService.refreshToken(account);
            } catch (Exception e) {
                LOG.error("刷新令牌错误", e);
                asyncRunner.runOnUI(() ->
                        NotificationManager.message(MessageInfo.warning("塔吉多账号登录失效", e.getMessage()))
                );
            }
        }
    }

    public void autoTaygedoSign() {
        if (Config.getSetting().isTaygedoAutoSign()) {
            List<TaygedoAccount> accounts = accountService.getNotSignedTodayList();
            if (accounts.isEmpty()) {
                return;
            }
            int success = 0;
            int fail = 0;
            for (int i = 0; i < accounts.size(); i++) {
                TaygedoAccount account = accounts.get(i);
                try {
                    SigninState signinState = signInService.getSigninState(account);
                    if (signinState.isTodaySign()) {
                        success++;
                        accountService.refreshLastSignTime(account);
                        continue;
                    }

                    signInService.gameSignin(account);
                    accountService.refreshLastSignTime(account);
                    success++;
                } catch (TaygedoException e) {
                    fail++;
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
            asyncRunner.runOnUI(() -> {
                String format = String.format("成功 %d, 失败 %d", finalSuccess, finalFail);
                NotificationManager.message(MessageInfo.success("自动签到完成", format));
            });
        }
    }

    public void checkVersionAndClean() {
        if (Config.getSetting().isCheckNewVersion()) {
            asyncRunner.runOnUI(() -> {
                CheckAppVersionTask task = new CheckAppVersionTask(true);
                task.setOnSucceeded(workerStateEvent -> {
                    ResponseBody<Release> value = task.getValue();
                    if (value.getCode() == 200) {
                        asyncRunner.runOnUI(() -> {
                            NotificationManager.publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
                        });
                    } else if (value.getCode() == -1) {
                        NotificationManager.publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
                        NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.main.message.type01")));
                    }
                });
                asyncRunner.runBackground(task);
            });
        }

        asyncRunner.runBackground(new DeleteOldAppVersionTask());
    }

}
