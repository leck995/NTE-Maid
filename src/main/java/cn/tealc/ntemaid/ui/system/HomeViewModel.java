package cn.tealc.ntemaid.ui.system;


import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewModel;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class HomeViewModel implements ViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(HomeViewModel.class);
    private SimpleStringProperty energyText = new SimpleStringProperty();
    private SimpleStringProperty energyTimeText = new SimpleStringProperty();
    private SimpleStringProperty storeEnergyText = new SimpleStringProperty();
    private SimpleStringProperty livenessText = new SimpleStringProperty();
    private SimpleStringProperty battlePassLevelText = new SimpleStringProperty();
    private SimpleStringProperty battlePassNumText = new SimpleStringProperty();
    private SimpleDoubleProperty battlePassProgress = new SimpleDoubleProperty();
    private SimpleBooleanProperty rolePaneVisible = new SimpleBooleanProperty(false);
    private SimpleStringProperty roleNameText = new SimpleStringProperty();
    private SimpleStringProperty gameLifeText = new SimpleStringProperty();
    private SimpleStringProperty levelText = new SimpleStringProperty();
    private SimpleStringProperty box1Text = new SimpleStringProperty();
    private SimpleStringProperty box2Text = new SimpleStringProperty();
    private SimpleStringProperty box3Text = new SimpleStringProperty();
    private SimpleStringProperty box4Text = new SimpleStringProperty();
    private SimpleStringProperty gameTimeText = new SimpleStringProperty();
    private SimpleStringProperty gameTimeTipText = new SimpleStringProperty();
    private SimpleObjectProperty<Image> headImg = new SimpleObjectProperty<>();
    private SimpleBooleanProperty hasSign = new SimpleBooleanProperty(true);
    private SimpleStringProperty signText = new SimpleStringProperty();
    private SimpleStringProperty weeklyRougeText = new SimpleStringProperty();
    private SimpleStringProperty weeklyRougeTipText = new SimpleStringProperty("肉鸽");
    private SimpleStringProperty weeklyInstCountText = new SimpleStringProperty();
    private SimpleStringProperty weeklyInstCountTipText = new SimpleStringProperty("周本");
    private SimpleBooleanProperty startGameBtnDisabled = new SimpleBooleanProperty(false);

    public HomeViewModel() {
//        updateGameTime(GameAppListener.getInstance().getDuration());
//        MvvmFX.getNotificationCenter().subscribe(NotificationKey.HOME_GAME_TIME_UPDATE, (s, objects) -> {
//            if (objects.length > 0) {
//                long playTime = (long) objects[0];
//                updateGameTime(playTime);
//            } else {
//                updateGameTime(0);
//            }
//        });
//        MvvmFX.getNotificationCenter().subscribe(NotificationKey.HOME_ROLE_DATA_REFRESH, (s, objects) -> {
//            updateKujiequRoleData();
//            autoSign(); //如果结束游戏跨天，直接签到
//        });
    }




/**
     * @return void
     * @description: 更新游玩时长；会对数据库与time进行相加处理，并显示
     * @param: time    尚未保存到数据库中的时长
     * @date: 2024/10/8


    private void updateGameTime(long time) {
        List<GameTime> list = getGameTimes();
        if (list != null) {
            long sum = list.stream().mapToLong(GameTime::getDuration).sum() + time;
            updateGameTimeText(sum);
        }
    }*/

/**
     * @return java.util.List<cn.tealc.wutheringwavestool.model.game.GameTime>
     * @description: 获取数据库中的当天游玩时长时间
     * @date: 2024/10/8


    private List<GameTime> getGameTimes() {
        GameTimeDao gameTimeDao = new GameTimeDao();
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateTimeFormatter.format(localDate);
        return gameTimeDao.getTimeListByData(date);
    }*/

/*
    private void updateGameTimeText(long sum) {
        int hour = (int) (sum / (1000 * 60 * 60));
        int minute = (int) ((sum % (1000 * 60 * 60)) / (1000 * 60));
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

        String total = LanguageManager.getString("ui.home.label.time.total");
        gameTimeText.set(String.format(total, hour, minute));
    }
*/









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
                            MessageInfo.warning(  String.format(
                                    LanguageManager.getString("ui.home.message.type05"),
                                    exe.getPath()
                            )));
                    return;
                }
            }else{
                exe = new File(dir,"NTELauncher.exe");
                if (!exe.exists()) {
                    MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE,
                            MessageInfo.warning(  String.format(
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
      /*  if (Config.setting.isHideWhenGameStart()) {
            MainApplication.window.hide();
        }*/
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
                //GameAppListener.getInstance().setStartFromApp(true);
                processBuilder.start();
            } catch (IOException e) {
                //MainApplication.window.show();
                //GameAppListener.getInstance().setStartFromApp(false);
                LOG.error("高级启动无法启动鸣潮", e);
                MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE, MessageInfo.error(LanguageManager.getString("ui.home.message.type07") + e.getMessage()));
            }
        });
    }









    public String getEnergyText() {
        return energyText.get();
    }

    public void setEnergyText(String energyText) {
        this.energyText.set(energyText);
    }

    public SimpleStringProperty energyTextProperty() {
        return energyText;
    }

    public String getLivenessText() {
        return livenessText.get();
    }

    public void setLivenessText(String livenessText) {
        this.livenessText.set(livenessText);
    }

    public SimpleStringProperty livenessTextProperty() {
        return livenessText;
    }

    public String getBattlePassLevelText() {
        return battlePassLevelText.get();
    }

    public void setBattlePassLevelText(String battlePassLevelText) {
        this.battlePassLevelText.set(battlePassLevelText);
    }

    public SimpleStringProperty battlePassLevelTextProperty() {
        return battlePassLevelText;
    }

    public String getBattlePassNumText() {
        return battlePassNumText.get();
    }

    public void setBattlePassNumText(String battlePassNumText) {
        this.battlePassNumText.set(battlePassNumText);
    }

    public SimpleStringProperty battlePassNumTextProperty() {
        return battlePassNumText;
    }

    public double getBattlePassProgress() {
        return battlePassProgress.get();
    }

    public void setBattlePassProgress(double battlePassProgress) {
        this.battlePassProgress.set(battlePassProgress);
    }

    public SimpleDoubleProperty battlePassProgressProperty() {
        return battlePassProgress;
    }

    public String getEnergyTimeText() {
        return energyTimeText.get();
    }

    public void setEnergyTimeText(String energyTimeText) {
        this.energyTimeText.set(energyTimeText);
    }

    public SimpleStringProperty energyTimeTextProperty() {
        return energyTimeText;
    }

    public String getRoleNameText() {
        return roleNameText.get();
    }

    public void setRoleNameText(String roleNameText) {
        this.roleNameText.set(roleNameText);
    }

    public SimpleStringProperty roleNameTextProperty() {
        return roleNameText;
    }

    public String getGameLifeText() {
        return gameLifeText.get();
    }

    public void setGameLifeText(String gameLifeText) {
        this.gameLifeText.set(gameLifeText);
    }

    public SimpleStringProperty gameLifeTextProperty() {
        return gameLifeText;
    }

    public String getLevelText() {
        return levelText.get();
    }

    public void setLevelText(String levelText) {
        this.levelText.set(levelText);
    }

    public SimpleStringProperty levelTextProperty() {
        return levelText;
    }

    public String getBox1Text() {
        return box1Text.get();
    }

    public void setBox1Text(String box1Text) {
        this.box1Text.set(box1Text);
    }

    public SimpleStringProperty box1TextProperty() {
        return box1Text;
    }

    public String getBox2Text() {
        return box2Text.get();
    }

    public void setBox2Text(String box2Text) {
        this.box2Text.set(box2Text);
    }

    public SimpleStringProperty box2TextProperty() {
        return box2Text;
    }

    public String getBox3Text() {
        return box3Text.get();
    }

    public void setBox3Text(String box3Text) {
        this.box3Text.set(box3Text);
    }

    public SimpleStringProperty box3TextProperty() {
        return box3Text;
    }

    public String getBox4Text() {
        return box4Text.get();
    }

    public void setBox4Text(String box4Text) {
        this.box4Text.set(box4Text);
    }

    public SimpleStringProperty box4TextProperty() {
        return box4Text;
    }

    public Image getHeadImg() {
        return headImg.get();
    }

    public void setHeadImg(Image headImg) {
        this.headImg.set(headImg);
    }

    public SimpleObjectProperty<Image> headImgProperty() {
        return headImg;
    }

    public boolean isRolePaneVisible() {
        return rolePaneVisible.get();
    }

    public SimpleBooleanProperty rolePaneVisibleProperty() {
        return rolePaneVisible;
    }

    public String getGameTimeText() {
        return gameTimeText.get();
    }

    public void setGameTimeText(String gameTimeText) {
        this.gameTimeText.set(gameTimeText);
    }

    public SimpleStringProperty gameTimeTextProperty() {
        return gameTimeText;
    }

    public String getGameTimeTipText() {
        return gameTimeTipText.get();
    }

    public SimpleStringProperty gameTimeTipTextProperty() {
        return gameTimeTipText;
    }

    public boolean isHasSign() {
        return hasSign.get();
    }

    public void setHasSign(boolean hasSign) {
        this.hasSign.set(hasSign);
    }

    public SimpleBooleanProperty hasSignProperty() {
        return hasSign;
    }

    public String getStoreEnergyText() {
        return storeEnergyText.get();
    }

    public SimpleStringProperty storeEnergyTextProperty() {
        return storeEnergyText;
    }

    public String getWeeklyInstCountText() {
        return weeklyInstCountText.get();
    }

    public SimpleStringProperty weeklyInstCountTextProperty() {
        return weeklyInstCountText;
    }

    public boolean isStartGameBtnDisabled() {
        return startGameBtnDisabled.get();
    }

    public SimpleBooleanProperty startGameBtnDisabledProperty() {
        return startGameBtnDisabled;
    }

    public String getWeeklyRougeText() {
        return weeklyRougeText.get();
    }

    public SimpleStringProperty weeklyRougeTextProperty() {
        return weeklyRougeText;
    }

    public void setWeeklyRougeText(String weeklyRougeText) {
        this.weeklyRougeText.set(weeklyRougeText);
    }

    public String getWeeklyRougeTipText() {
        return weeklyRougeTipText.get();
    }

    public SimpleStringProperty weeklyRougeTipTextProperty() {
        return weeklyRougeTipText;
    }

    public String getWeeklyInstCountTipText() {
        return weeklyInstCountTipText.get();
    }

    public SimpleStringProperty weeklyInstCountTipTextProperty() {
        return weeklyInstCountTipText;
    }
}
