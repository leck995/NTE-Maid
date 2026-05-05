package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.model.system.nav.NavData;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.saxsys.mvvmfx.ViewModel;
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
        checkVersion();
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


    public void checkVersion() {
/*        if (Config.setting.isCheckNewVersion()) {
            Platform.runLater(() -> {
                CheckVersionTask task = new CheckVersionTask(true);
                task.setOnSucceeded(workerStateEvent -> {
                    ResponseBody<Release> value = task.getValue();
                    if (value.getCode() == 200) {
                        Platform.runLater(() -> {
                            MvvmFX.getNotificationCenter().publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
                        });
                    } else if (value.getCode() == -1) {
                        MvvmFX.getNotificationCenter().publish(NotificationKey.NOTIFICATION_SHOW_UPDATE, value.getData());
                        MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE, new MessageInfo(MessageType.WARNING, LanguageManager.getString("ui.main.message.type01")));
                    }
                });
                Thread.startVirtualThread(task);
            });
        }*/
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