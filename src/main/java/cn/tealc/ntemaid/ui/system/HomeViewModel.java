package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.dao.GameTimeDao;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.model.game.GameTime;
import cn.tealc.ntemaid.service.GameTimeService;
import cn.tealc.ntemaid.service.impl.GameTimeServiceImpl;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class HomeViewModel implements ViewModel, SceneLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(HomeViewModel.class);
    private final GameTimeService gameTimeService = new GameTimeServiceImpl();

    private final SimpleStringProperty gameTimeText = new SimpleStringProperty();
    private final SimpleStringProperty gameTimeTipText = new SimpleStringProperty();
    private final SimpleBooleanProperty startGameBtnDisabled = new SimpleBooleanProperty(false);
    private final NotificationObserver gameTimeObserver;
    public HomeViewModel() {
        updateGameTime(GameAppListener.getInstance().getDuration());

        gameTimeObserver = (s, objects) -> {
            long playTime = (objects != null && objects.length > 0) ? (long) objects[0] : 0;
            updateGameTime(playTime);
        };
        NotificationManager.subscribe(NotificationKey.HOME_GAME_TIME_UPDATE, gameTimeObserver);
    }


    /**
     * 更新游玩时长文本和提示
     * @param runningTime 尚未保存到数据库中的当前会话时长（毫秒）
     */
    private void updateGameTime(long runningTime) {
        // 直接从 Service 获取今日已存数据库的总时长
        long savedTime = gameTimeService.getTodayTotalDuration();
        long totalSum = savedTime + runningTime;

        updateGameTimeUI(totalSum);
    }
    private void updateGameTimeUI(long totalMillis) {
        java.time.Duration duration = java.time.Duration.ofMillis(totalMillis);
        long hour = duration.toHours();
        long minute = duration.toMinutesPart(); // Java 9+ 使用 toMinutesPart，Java 8 请用 toMinutes() % 60

        // 更新提示语逻辑
        String[] tips = LanguageManager.getStringArray("ui.home.label.time.others");
        if (hour == 0 && minute == 0) {
            gameTimeTipText.set(tips[0]);
        } else if (hour < 1 && minute < 15) {
            gameTimeTipText.set(tips[1]);
        } else if (hour <= 2) {
            gameTimeTipText.set(tips[2]);
        } else if (hour <= 5) {
            gameTimeTipText.set(tips[3]);
        } else {
            gameTimeTipText.set(tips[4]);
        }

        // 更新时长文本
        String format = LanguageManager.getString("ui.home.label.time.total");
        gameTimeText.set(String.format(format, hour, minute));
    }






    public void checkIsWeekEnd() {
/*        if (Config.setting.getGameRootDirSource() == SourceType.GLOBAL) {
            Platform.runLater(() -> {
                onWeekEnd(true, null);
            });
        }*/
    }





/*    public void startUpdate() {
        if (Config.setting.getGameRootDirSource() == SourceType.WE_GAME) {
            MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                    new MessageInfo(MessageType.WARNING, LanguageManager.getString("ui.home.message.type02")), false);
        } else {
            String dir = Config.setting.getGameRootDir();
            if (dir != null) {
                File gameDir = GameResourcesManager.getGameDir();
                if (gameDir != null) {
                    File parent = gameDir.getParentFile();
                    File exe = new File(parent, "launcher.exe");
                    if (exe.exists()) {
                        try {
                            Desktop.getDesktop().open(exe);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                                new MessageInfo(MessageType.WARNING, String.format(LanguageManager.getString("ui.home.message.type08"), exe.getPath()), false));
                    }
                } else {
                    MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                            new MessageInfo(MessageType.WARNING, LanguageManager.getString("ui.home.message.type08")), false);
                }
            } else {
                MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                        new MessageInfo(MessageType.WARNING, LanguageManager.getString("ui.home.message.type08")), false);
            }
        }
    }*/


    /*
     * 启动，先删除旧日志，然后判断是否启动参数，并进行启动
     * */
    public void startGame() {
        //先设置1s的禁止点击，防止双击启动
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(1));
        startGameBtnDisabled.set(true);
        pauseTransition.setOnFinished(event -> {
            startGameBtnDisabled.set(false);
        });
        pauseTransition.play();

        //删除游戏过去的日志，避免数据污染
        //deleteLogFiles();

        String dir = Config.setting.getGameRootDir();
        if (dir != null) {
            File exe = null;
            //当自定义启动程序时
            if (Config.setting.isGameStartAppCustom()) {
                exe = new File(Config.setting.getGameStarAppPath());
                if (!exe.exists()) {
                    MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                            MessageInfo.warning(String.format(
                                    LanguageManager.getString("ui.home.message.type05"),
                                    exe.getPath()
                            )));
                    return;
                }
            } else {
                exe = new File(dir, "NTELauncher.exe");
                if (!exe.exists()) {
                    MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
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
            hideMainWindow();
        } else {
            MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                    MessageInfo.warning(LanguageManager.getString("ui.home.message.type04")));
        }
    }


    //启动时隐藏窗口
    private void hideMainWindow() {
        if (Config.setting.isHideWhenGameStart()) {
            NotificationManager.publish(NotificationKey.APP_HIDE);
        }
    }


    //第一个参数必须是启动器的路径
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
                LOG.error("高级启动无法启动异环", e);
                MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE, MessageInfo.error(LanguageManager.getString("ui.home.message.type07") + e.getMessage()));
            }
        });
    }


    public String getGameTimeText() {
        return gameTimeText.get();
    }

    public SimpleStringProperty gameTimeTextProperty() {
        return gameTimeText;
    }

    public void setGameTimeText(String gameTimeText) {
        this.gameTimeText.set(gameTimeText);
    }

    public String getGameTimeTipText() {
        return gameTimeTipText.get();
    }

    public SimpleStringProperty gameTimeTipTextProperty() {
        return gameTimeTipText;
    }

    public void setGameTimeTipText(String gameTimeTipText) {
        this.gameTimeTipText.set(gameTimeTipText);
    }

    public boolean isStartGameBtnDisabled() {
        return startGameBtnDisabled.get();
    }

    public SimpleBooleanProperty startGameBtnDisabledProperty() {
        return startGameBtnDisabled;
    }

    public void setStartGameBtnDisabled(boolean startGameBtnDisabled) {
        this.startGameBtnDisabled.set(startGameBtnDisabled);
    }

    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {
        NotificationManager.unsubscribe(NotificationKey.HOME_GAME_TIME_UPDATE,gameTimeObserver);

    }
}
