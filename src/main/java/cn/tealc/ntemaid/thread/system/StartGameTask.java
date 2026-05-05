package cn.tealc.ntemaid.thread.system;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.MvvmFX;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StartGameTask extends Task<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(StartGameTask.class);

    @Override
    protected Boolean call() throws Exception {
        startGame();
        return true;
    }

    /*
     * 启动，先删除旧日志，然后判断是否启动参数，并进行启动
     * */
    public void startGame() {
        String dir = Config.setting.getGameRootDir();
        if (dir != null) {
            File exe = null;
            //当自定义启动程序时
            if (Config.setting.isGameStartAppCustom()) {
                exe = new File(Config.setting.getGameStarAppPath());
                if (!exe.exists()) {
                    NotificationManager.message(
                            MessageInfo.warning(String.format(
                                    LanguageManager.getString("ui.home.message.type05"),
                                    exe.getPath()
                            )));
                    return;
                }
            } else {
                exe = new File(dir, "NTELauncher.exe");
                if (!exe.exists()) {
                    NotificationManager.message(
                            MessageInfo.warning(String.format(
                                    LanguageManager.getString("ui.home.message.type03"),
                                    exe.getPath()
                            )));
                    return;
                }
            }
            List<String> paramsList = new ArrayList<String>(Config.setting.getStartUpParams());
            paramsList.addFirst(exe.getAbsolutePath());
            String[] newArray = new String[paramsList.size()];
            paramsList.toArray(newArray);
            runExeByCustom(newArray);

            if (Config.setting.isHideWhenGameStart()) {
                NotificationManager.publish(NotificationKey.APP_HIDE);
            }

        } else {
            NotificationManager.message(
                    MessageInfo.warning(LanguageManager.getString("ui.home.message.type04")));
        }
    }



    private void runExeByCustom(String... params) {
        Thread.startVirtualThread(() -> {
            String[] command2 = {"cmd.exe", "/c", "start", "\"\""}; //权限不够，提权
            String[] mergedArray = Stream.concat(Stream.of(command2), Stream.of(params))
                    .toArray(String[]::new);
            ProcessBuilder processBuilder = new ProcessBuilder(mergedArray);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            try {
                processBuilder.start();
            } catch (IOException e) {
                log.error("高级启动无法启动异环", e);
                NotificationManager.message(MessageInfo.error(LanguageManager.getString("ui.home.message.type07") + e.getMessage()));
            }
        });
    }
}
