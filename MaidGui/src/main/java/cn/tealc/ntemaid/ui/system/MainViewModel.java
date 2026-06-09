package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.nav.NavData;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.ntemaid.player.MusicPlayerClient;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @program: WutheringWavesTool
 * @description:
 * @author: Leck
 * @create: 2024-07-03 18:59
 */
public class MainViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(MainViewModel.class);
    private final AtomicBoolean warningTower = new AtomicBoolean(false);
    private final AtomicBoolean warningSlash = new AtomicBoolean(false);

    public MainViewModel() {
        checkVersionAndClean();
        checkGameLogOpen();
        initMusicClient();
        startTaygedoTask();
    }

    private static void initMusicClient() {
        MusicPlayerClient.getInstance().init();
    }

    public List<NavData> getNavList(){
        InputStream inputStream = FXResourcesLoader.loadStream("/cn/tealc/ntemaid/data/nav.json");
        ObjectMapper mapper = new ObjectMapper();
        List<NavData> list = null;
        try {
            list = mapper.readValue(inputStream, new TypeReference<List<NavData>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }


    public void startTaygedoTask(){
        Thread.startVirtualThread(()->{
            taygedoRefrshToken();
            autoTaygedoSign();
        });
    }

    /**
     * 刷新塔吉多账号令牌
     *
     *
     * @author leck
     * @date 2026/06/09
     */
    private void taygedoRefrshToken(){
        TaygedoAccountService accountService = new TaygedoAccountService();
        TaygedoLoginService loginService = new TaygedoLoginService();
        List<TaygedoAccount> accounts = accountService.getAll();
        for (TaygedoAccount account : accounts) {
            try {
                loginService.refreshToken(account);
            }catch (Exception e){
                LOG.error("刷新令牌错误",e);
                Platform.runLater(()->
                        NotificationManager.message(MessageInfo.warning("塔吉多账号登录失效",e.getMessage()))
                );
            }
        }

    }


    /**
     * 塔吉多签到
     *
     *
     * @author leck
     * @date 2026/06/09
     */
    public void autoTaygedoSign(){
        if (Config.setting.isTaygedoAutoSign()){
            TaygedoSignInService signInService = new TaygedoSignInService();
            TaygedoAccountService accountService = new TaygedoAccountService();
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
                    if (signinState.isTodaySign()){
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
            Platform.runLater(() -> {
                String format = String.format("成功 %d, 失败 %d", finalSuccess, finalFail);
                NotificationManager.message(MessageInfo.success("自动签到完成",format));
            });
        }
    }



    public void checkVersionAndClean() {
        if (Config.setting.isCheckNewVersion()) {
            Platform.runLater(() -> {
                CheckAppVersionTask task = new CheckAppVersionTask(true);
                task.setOnSucceeded(workerStateEvent -> {
                    ResponseBody<Release> value = task.getValue();
                    if (value.getCode() == 200) {
                        Platform.runLater(() -> {
                            NotificationManager.publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
                        });
                    } else if (value.getCode() == -1) {
                        NotificationManager.publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
                        NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.main.message.type01")));
                    }
                });
                Thread.startVirtualThread(task);
            });
        }

        Thread.startVirtualThread(new DeleteOldAppVersionTask());
    }


    private void checkGameLogOpen() {
/*        CheckGameConfigTask task = new CheckGameConfigTask();
        task.setOnSucceeded(workerStateEvent -> {
            Boolean value = task.getValue();
            if (!value) { //游戏日志可能被关闭了
                Platform.runLater(() -> {
                    NotificationManager.message(MessageInfo.success(LanguageManager.getString("ui.main.sync.message.log.close")));
                });
            }
        });
        Thread.startVirtualThread(task);*/
    }







}