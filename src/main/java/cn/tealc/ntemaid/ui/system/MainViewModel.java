package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.nav.NavData;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.thread.system.update.CheckAppVersionTask;
import cn.tealc.ntemaid.thread.system.update.DeleteOldAppVersionTask;
import cn.tealc.ntemaid.util.LanguageManager;
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