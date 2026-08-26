package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.AnnouncementItem;
import cn.tealc.ntemaid.model.system.ResponseBody;
import cn.tealc.ntemaid.model.system.nav.NavData;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.repository.NavRepository;
import cn.tealc.ntemaid.service.*;
import cn.tealc.ntemaid.service.system.ConfigService;
import cn.tealc.ntemaid.service.taygedo.TaygedoAccountService;
import cn.tealc.ntemaid.service.taygedo.TaygedoLoginService;
import cn.tealc.ntemaid.service.taygedo.TaygedoSignInService;
import cn.tealc.ntemaid.thread.system.AnnouncementGetTask;
import cn.tealc.ntemaid.thread.system.resources.AppResourcesSyncTask;
import cn.tealc.ntemaid.thread.system.update.CheckAppVersionTask;
import cn.tealc.ntemaid.thread.system.update.DeleteOldAppVersionTask;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.SigninState;
import cn.tealc.teafx.utils.message.MessageInfo;
import cn.tealc.teafx.utils.message.MessageType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description:
 * @author: Leck
 * @create: 2024-07-03 18:59
 */
public class MainViewModel implements ViewModel, SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(MainViewModel.class);
    private static final String ANNOUNCEMENTS = "ANNOUNCEMENTS";
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
        syncAppResources();
        checkAnnouncements();
    }

    private static void initMusicClient() {
        MusicPlayerClient.getInstance().init();
    }

    public List<NavData> getNavList() {
        return navRepo.load();
    }


    private void syncAppResources() {
        AppResourcesSyncTask task = new AppResourcesSyncTask();
        task.messageProperty().addListener((observableValue, s, t1) -> {
            if (t1 != null) {
                String title = LanguageManager.getString("ui.main.sync.title");
                switch (t1) {
                    case "success" -> MvvmFX.getNotificationCenter().publish(
                            NotificationKey.MESSAGE,
                            MessageInfo.success(title,LanguageManager.getString("ui.main.sync.message.success")));
                    case "error" -> MvvmFX.getNotificationCenter().publish(
                            NotificationKey.MESSAGE,
                            MessageInfo.error(title,LanguageManager.getString("ui.main.sync.message.error")));
                    case "start" -> MvvmFX.getNotificationCenter().publish(
                            NotificationKey.MESSAGE,
                            MessageInfo.info(title,LanguageManager.getString("ui.main.sync.message.start")));
                }
            }
        });
        asyncRunner.runBackground(task);
    }



    public void startTaygedoTask() {
        if (!Config.getSetting().isEnableTaygedo())
            return;
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
                        NotificationManager.message(MessageInfo.warning(account.getName() + " 自动登录失败","原因：" + e.getMessage()))
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
                            NotificationManager.publish(NotificationKey.APP_SHOW);
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

    private void checkAnnouncements() {
        String gameId = "nte2001";
        AnnouncementGetTask task = new AnnouncementGetTask(gameId);
        task.setOnSucceeded(workerStateEvent -> {
            ResponseBody<List<AnnouncementItem>> value = task.getValue();
            if (value.getCode() == 200 && value.getData() != null) {
                ConfigService configService = AppInjector.getInstance(ConfigService.class);
                Set<Integer> notifiedIds = configService.getObjectConfig(ANNOUNCEMENTS, new TypeReference<Set<Integer>>() {})
                        .orElse(new HashSet<>());
                for (AnnouncementItem item : value.getData()) {
                    if (!notifiedIds.contains(item.getId())) {
                        Platform.runLater(() -> {
                            MessageInfo messageInfo = new MessageInfo(MessageType.INFO,item.getTitle(), item.getContent(),true, Duration.seconds(30));
                            NotificationManager.message(messageInfo);
                        });
                        notifiedIds.add(item.getId());
                    }
                }
                configService.setObject(ANNOUNCEMENTS, notifiedIds);
            }
        });
        asyncRunner.runBackground(task);
    }


}
