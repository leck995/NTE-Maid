package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.util.GameClientType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @description:
 * @author: Leck
 * @create: 2024-07-03 00:38
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Setting {
    private final SimpleBooleanProperty devModel = new SimpleBooleanProperty(false); //开发者模式。用于测试
    private final SimpleObjectProperty<Locale> language = new SimpleObjectProperty<>(Locale.CHINA);
    private final SimpleDoubleProperty appWidth = new SimpleDoubleProperty(1280.0);
    private final SimpleDoubleProperty appHeight = new SimpleDoubleProperty(760.0);
    private final SimpleIntegerProperty uiScale = new SimpleIntegerProperty(100);

    private final SimpleBooleanProperty leftBarShow = new SimpleBooleanProperty(true); //左侧菜单栏是否关闭
    private final SimpleBooleanProperty theme = new SimpleBooleanProperty(false); //主题，false为亮色
    private final SimpleBooleanProperty support = new SimpleBooleanProperty(false);  //标志是否赞助

    private final SimpleStringProperty homeViewIcon = new SimpleStringProperty();  //主页头像
    private final SimpleStringProperty homeViewRole = new SimpleStringProperty(); //主页人物
    private final SimpleStringProperty logLevel = new SimpleStringProperty("DEBUG"); //日志等级

    private final SimpleStringProperty skipVersion = new SimpleStringProperty(Config.version);

    /*=================设置-首选===================*/
    private final SimpleObjectProperty<GameClientType> gameRootDirSource = new SimpleObjectProperty<>(GameClientType.DEFAULT); //游戏来源类型
    private final SimpleStringProperty gameRootDir = new SimpleStringProperty();//游戏根目录
    private final SimpleStringProperty gameStarAppPath = new SimpleStringProperty("NTELauncher.exe");//游戏启动文件
    private final SimpleBooleanProperty gameStartAppCustom = new SimpleBooleanProperty(false); //自定义启动程序

    /*=================设置-基础设置===================*/
    private final SimpleBooleanProperty diyHomeBg = new SimpleBooleanProperty(false); //启用自定义背景
    private final SimpleStringProperty diyHomeBgName = new SimpleStringProperty(); //自定义背景文件名称
    private final SimpleIntegerProperty diyHomeBgType = new SimpleIntegerProperty(); // 0为默认，1为指定背景，2为背景文件夹
    private final SimpleStringProperty diyHomeBgDir = new SimpleStringProperty();

    private final SimpleIntegerProperty closeEvent = new SimpleIntegerProperty(0); //关闭主界面行为，0选择，1退出，2最小化
    /*=================设置-游戏行为===================*/
    private final SimpleBooleanProperty exitWhenGameOver = new SimpleBooleanProperty(false); //检测到游戏关闭自动关闭程序
    private final SimpleBooleanProperty autoKillOfficialLauncher = new SimpleBooleanProperty(false); //自动关闭官方启动器
    private final SimpleBooleanProperty hideWhenGameStart = new SimpleBooleanProperty(false); //检测到游戏启动自动隐藏程序至托盘
    private final SimpleBooleanProperty silentStartup = new SimpleBooleanProperty(false); //静默启动
    private final SimpleBooleanProperty autoStartGame = new SimpleBooleanProperty(false); //启动时同时启动游戏
    /*=================设置-其他设置===================*/
    private final SimpleBooleanProperty checkNewVersion = new SimpleBooleanProperty(true); //检查更新
    private final SimpleBooleanProperty taygedoAutoSign = new SimpleBooleanProperty(false); //塔吉多自动签到

    /*=============资源库=============*/
    private final SimpleIntegerProperty resourceSource = new SimpleIntegerProperty(getLanguage() == Locale.CHINA ? 1 : 0); //0代表Github，1代表码云或其他

    /*=================高级启动相关===================*/
    private final SimpleBooleanProperty userAdvanceGameSettings = new SimpleBooleanProperty(false); //使用高级启动
    private final SimpleStringProperty appParams = new SimpleStringProperty(); //启动参数
    @JsonSerialize(using = ObservableListSerializer.class)
    @JsonDeserialize(using = ObservableListDeserializer.class)
    private ObservableList<String> startUpParams = FXCollections.observableArrayList();

    /*=================播放器相关===================*/
    private final SimpleStringProperty musicDir = new SimpleStringProperty(); //歌曲目录
    private final SimpleDoubleProperty musicVolume = new SimpleDoubleProperty(0.5); //歌曲目录
    private final SimpleBooleanProperty musicEnable = new SimpleBooleanProperty(true); //启动游戏音乐播放功能


    /*=================钓鱼相关===================*/
    private final SimpleBooleanProperty fishing = new SimpleBooleanProperty(true);//开启钓鱼优化
    private final SimpleBooleanProperty fishingBait = new SimpleBooleanProperty(true);//自动拉杆
    private final SimpleBooleanProperty fishingFinish = new SimpleBooleanProperty(true);//退出结算界面




    // 自定义序列化器
    public static class ObservableListSerializer extends JsonSerializer<ObservableList<String>> {
        @Override
        public void serialize(ObservableList<String> value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers)
                throws IOException {
            gen.writeStartArray();
            for (String item : value) {
                gen.writeString(item);
            }
            gen.writeEndArray();
        }
    }

    // 自定义反序列化器
    public static class ObservableListDeserializer extends JsonDeserializer<ObservableList<String>> {
        @Override
        @SuppressWarnings("unchecked")
        public ObservableList<String> deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt)
                throws IOException {
            List<String> list = p.readValueAs(List.class);
            return FXCollections.observableArrayList(list);
        }
    }

    public Locale getLanguage() {
        return language.get();
    }

    public SimpleObjectProperty<Locale> languageProperty() {
        return language;
    }

    public void setLanguage(Locale language) {
        this.language.set(language);
    }

    public String getGameRootDir() {
        return gameRootDir.get();
    }

    public SimpleStringProperty gameRootDirProperty() {
        return gameRootDir;
    }

    public void setGameRootDir(String gameRootDir) {
        this.gameRootDir.set(gameRootDir);
    }

    public boolean isTheme() {
        return theme.get();
    }

    public SimpleBooleanProperty themeProperty() {
        return theme;
    }

    public void setTheme(boolean theme) {
        this.theme.set(theme);
    }

    public String getHomeViewIcon() {
        return homeViewIcon.get();
    }

    public SimpleStringProperty homeViewIconProperty() {
        return homeViewIcon;
    }

    public void setHomeViewIcon(String homeViewIcon) {
        this.homeViewIcon.set(homeViewIcon);
    }

    public String getHomeViewRole() {
        return homeViewRole.get();
    }

    public SimpleStringProperty homeViewRoleProperty() {
        return homeViewRole;
    }

    public void setHomeViewRole(String homeViewRole) {
        this.homeViewRole.set(homeViewRole);
    }

    public boolean isExitWhenGameOver() {
        return exitWhenGameOver.get();
    }

    public SimpleBooleanProperty exitWhenGameOverProperty() {
        return exitWhenGameOver;
    }

    public void setExitWhenGameOver(boolean exitWhenGameOver) {
        this.exitWhenGameOver.set(exitWhenGameOver);
    }

    public boolean isHideWhenGameStart() {
        return hideWhenGameStart.get();
    }

    public SimpleBooleanProperty hideWhenGameStartProperty() {
        return hideWhenGameStart;
    }

    public void setHideWhenGameStart(boolean hideWhenGameStart) {
        this.hideWhenGameStart.set(hideWhenGameStart);
    }

    public boolean isDiyHomeBg() {
        return diyHomeBg.get();
    }

    public SimpleBooleanProperty diyHomeBgProperty() {
        return diyHomeBg;
    }

    public void setDiyHomeBg(boolean diyHomeBg) {
        this.diyHomeBg.set(diyHomeBg);
    }

    public String getDiyHomeBgName() {
        return diyHomeBgName.get();
    }

    public SimpleStringProperty diyHomeBgNameProperty() {
        return diyHomeBgName;
    }

    public void setDiyHomeBgName(String diyHomeBgName) {
        this.diyHomeBgName.set(diyHomeBgName);
    }

    public double getAppWidth() {
        return appWidth.get();
    }

    public SimpleDoubleProperty appWidthProperty() {
        return appWidth;
    }

    public void setAppWidth(double appWidth) {
        this.appWidth.set(appWidth);
    }

    public double getAppHeight() {
        return appHeight.get();
    }

    public SimpleDoubleProperty appHeightProperty() {
        return appHeight;
    }

    public void setAppHeight(double appHeight) {
        this.appHeight.set(appHeight);
    }

    public GameClientType getGameRootDirSource() {
        return gameRootDirSource.get();
    }

    public SimpleObjectProperty<GameClientType> gameRootDirSourceProperty() {
        return gameRootDirSource;
    }

    public void setGameRootDirSource(GameClientType gameRootDirSource) {
        this.gameRootDirSource.set(gameRootDirSource);
    }

    public String getLogLevel() {
        return logLevel.get();
    }

    public SimpleStringProperty logLevelProperty() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel.set(logLevel);
    }

    public boolean isCheckNewVersion() {
        return checkNewVersion.get();
    }

    public SimpleBooleanProperty checkNewVersionProperty() {
        return checkNewVersion;
    }

    public void setCheckNewVersion(boolean checkNewVersion) {
        this.checkNewVersion.set(checkNewVersion);
    }

    public boolean isLeftBarShow() {
        return leftBarShow.get();
    }

    public SimpleBooleanProperty leftBarShowProperty() {
        return leftBarShow;
    }

    public void setLeftBarShow(boolean leftBarShow) {
        this.leftBarShow.set(leftBarShow);
    }

    public int getResourceSource() {
        return resourceSource.get();
    }

    public SimpleIntegerProperty resourceSourceProperty() {
        return resourceSource;
    }

    public void setResourceSource(int resourceSource) {
        this.resourceSource.set(resourceSource);
    }

    public String getGameStarAppPath() {
        return gameStarAppPath.get();
    }

    public SimpleStringProperty gameStarAppPathProperty() {
        return gameStarAppPath;
    }

    public void setGameStarAppPath(String gameStarAppPath) {
        this.gameStarAppPath.set(gameStarAppPath);
    }

    public boolean isGameStartAppCustom() {
        return gameStartAppCustom.get();
    }

    public SimpleBooleanProperty gameStartAppCustomProperty() {
        return gameStartAppCustom;
    }

    public void setGameStartAppCustom(boolean gameStartAppCustom) {
        this.gameStartAppCustom.set(gameStartAppCustom);
    }

    public boolean isSupport() {
        return support.get();
    }

    public SimpleBooleanProperty supportProperty() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support.set(support);
    }

    public boolean isUserAdvanceGameSettings() {
        return userAdvanceGameSettings.get();
    }

    public SimpleBooleanProperty userAdvanceGameSettingsProperty() {
        return userAdvanceGameSettings;
    }

    public void setUserAdvanceGameSettings(boolean userAdvanceGameSettings) {
        this.userAdvanceGameSettings.set(userAdvanceGameSettings);
    }

    public String getAppParams() {
        return appParams.get();
    }

    public SimpleStringProperty appParamsProperty() {
        return appParams;
    }

    public void setAppParams(String appParams) {
        this.appParams.set(appParams);
    }

    public int getCloseEvent() {
        return closeEvent.get();
    }

    public SimpleIntegerProperty closeEventProperty() {
        return closeEvent;
    }

    public void setCloseEvent(int closeEvent) {
        this.closeEvent.set(closeEvent);
    }

    public ObservableList<String> getStartUpParams() {
        return startUpParams;
    }

    public String getSkipVersion() {
        return skipVersion.get();
    }

    public SimpleStringProperty skipVersionProperty() {
        return skipVersion;
    }

    public void setSkipVersion(String skipVersion) {
        this.skipVersion.set(skipVersion);
    }

    public int getDiyHomeBgType() {
        return diyHomeBgType.get();
    }

    public SimpleIntegerProperty diyHomeBgTypeProperty() {
        return diyHomeBgType;
    }

    public void setDiyHomeBgType(int diyHomeBgType) {
        this.diyHomeBgType.set(diyHomeBgType);
    }

    public String getDiyHomeBgDir() {
        return diyHomeBgDir.get();
    }

    public SimpleStringProperty diyHomeBgDirProperty() {
        return diyHomeBgDir;
    }

    public void setDiyHomeBgDir(String diyHomeBgDir) {
        this.diyHomeBgDir.set(diyHomeBgDir);
    }

    public void setStartUpParams(ObservableList<String> startUpParams) {
        this.startUpParams = startUpParams;
    }

    public int getUiScale() {
        return uiScale.get();
    }

    public SimpleIntegerProperty uiScaleProperty() {
        return uiScale;
    }

    public void setUiScale(int uiScale) {
        this.uiScale.set(uiScale);
    }

    public boolean isDevModel() {
        return devModel.get();
    }

    public SimpleBooleanProperty devModelProperty() {
        return devModel;
    }

    public void setDevModel(boolean devModel) {
        this.devModel.set(devModel);
    }

    public String getMusicDir() {
        return musicDir.get();
    }

    public SimpleStringProperty musicDirProperty() {
        return musicDir;
    }

    public void setMusicDir(String musicDir) {
        this.musicDir.set(musicDir);
    }

    public double getMusicVolume() {
        return musicVolume.get();
    }

    public SimpleDoubleProperty musicVolumeProperty() {
        return musicVolume;
    }

    public void setMusicVolume(double musicVolume) {
        this.musicVolume.set(musicVolume);
    }

    public boolean isAutoStartGame() {
        return autoStartGame.get();
    }

    public SimpleBooleanProperty autoStartGameProperty() {
        return autoStartGame;
    }

    public void setAutoStartGame(boolean autoStartGame) {
        this.autoStartGame.set(autoStartGame);
    }

    public boolean isSilentStartup() {
        return silentStartup.get();
    }

    public SimpleBooleanProperty silentStartupProperty() {
        return silentStartup;
    }

    public void setSilentStartup(boolean silentStartup) {
        this.silentStartup.set(silentStartup);
    }

    public boolean isMusicEnable() {
        return musicEnable.get();
    }

    public SimpleBooleanProperty musicEnableProperty() {
        return musicEnable;
    }

    public void setMusicEnable(boolean musicEnable) {
        this.musicEnable.set(musicEnable);
    }

    public boolean isFishing() {
        return fishing.get();
    }

    public SimpleBooleanProperty fishingProperty() {
        return fishing;
    }

    public void setFishing(boolean fishing) {
        this.fishing.set(fishing);
    }

    public boolean isFishingBait() {
        return fishingBait.get();
    }

    public SimpleBooleanProperty fishingBaitProperty() {
        return fishingBait;
    }

    public void setFishingBait(boolean fishingBait) {
        this.fishingBait.set(fishingBait);
    }

    public boolean isFishingFinish() {
        return fishingFinish.get();
    }

    public SimpleBooleanProperty fishingFinishProperty() {
        return fishingFinish;
    }

    public void setFishingFinish(boolean fishingFinish) {
        this.fishingFinish.set(fishingFinish);
    }

    public boolean isAutoKillOfficialLauncher() {
        return autoKillOfficialLauncher.get();
    }

    public SimpleBooleanProperty autoKillOfficialLauncherProperty() {
        return autoKillOfficialLauncher;
    }

    public void setAutoKillOfficialLauncher(boolean autoKillOfficialLauncher) {
        this.autoKillOfficialLauncher.set(autoKillOfficialLauncher);
    }

    public boolean isTaygedoAutoSign() {
        return taygedoAutoSign.get();
    }

    public SimpleBooleanProperty taygedoAutoSignProperty() {
        return taygedoAutoSign;
    }

    public void setTaygedoAutoSign(boolean taygedoAutoSign) {
        this.taygedoAutoSign.set(taygedoAutoSign);
    }
}