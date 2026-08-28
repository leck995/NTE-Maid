package cn.tealc.ntemaid.ui.game.manage;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.service.system.GameServerService;
import cn.tealc.ntemaid.util.GameClientType;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @description:
 * @author: Leck
 * @create: 2025-03-08 23:11
 */
public class GameBaseSettingViewModel implements ViewModel, SceneLifecycle {
    private SimpleObjectProperty<GameClientType> gameClientType = new SimpleObjectProperty<>();
    private SimpleStringProperty gameDir=new SimpleStringProperty();
    private SimpleStringProperty gameAppStartPath=new SimpleStringProperty();
    private SimpleBooleanProperty gameAppStartCustom=new SimpleBooleanProperty();
    private SimpleBooleanProperty sourceTypeDisabled01=new SimpleBooleanProperty(true);
    private SimpleBooleanProperty sourceTypeDisabled02=new SimpleBooleanProperty(true);
    private SimpleBooleanProperty sourceTypeDisabled03=new SimpleBooleanProperty(true);
    private final ObservableList<String> startUpParams;


    public GameBaseSettingViewModel() {
        startUpParams = Config.getSetting().getStartUpParams();
    }


    public void init() {
        gameClientType.bindBidirectional(Config.getSetting().gameRootDirSourceProperty());
        gameDir.bindBidirectional(Config.getSetting().gameRootDirProperty());
        gameAppStartPath.bindBidirectional(Config.getSetting().gameStarAppPathProperty());
        gameAppStartCustom.bindBidirectional(Config.getSetting().gameStartAppCustomProperty());

        if (gameDir.get() == null){
            String gameInstallPath = getGameInstallPath();
            if (gameInstallPath != null){
                gameDir.set(gameInstallPath);
                String launcherName = AppInjector.getInstance(GameServerService.class).getLauncherFileName();
                gameAppStartPath.set(gameInstallPath + File.separator + launcherName);
            }
        }
    }





    /** 各服务器对应的注册表卸载项名（CN官服 YH、B服 YHBL、国际服 NTEGlobal） */
    private static final String[] UNINSTALL_KEYS = {"YH", "YHBL", "NTEGlobal"};

    /**
     * 从注册表自动探测游戏安装目录，逐个尝试各服务器的卸载项名。
     * 不依赖 GameServerService，避免与服务器判断的循环依赖。
     *
     * @return 游戏安装路径，未找到返回 null
     */
    public String getGameInstallPath() {
        String basePath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\";
        for (String key : UNINSTALL_KEYS) {
            String[] command = {"reg", "query", basePath + key, "/v", "InstallLocation"};

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            try {
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), Charset.forName("GBK")))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("REG_SZ")) {
                            String[] parts = line.split("REG_SZ");
                            if (parts.length > 1) {
                                String path = parts[1].trim();
                                System.out.println(path);
                                if (path.endsWith("\\"))
                                    return path.substring(0, path.length()-1);
                                else
                                    return path;
                            }
                        }
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                // 该注册表项不存在或查询失败，继续尝试下一个
            }
        }
        return null;
    }

    /**
     * 删除指定启动参数
     * @param index
     */
    public void deleteParam(int index) {
        startUpParams.remove(index);
    }

    /**
     * 添加启动参数
     * @param param
     */
    public void addParam(String param) {
        startUpParams.add(param);
    }

    public boolean isDx11(){
        return startUpParams.contains("-dx11");
    }
    public boolean isDx12(){
        return startUpParams.contains("-dx12");
    }

    /**
     * 启动参数中添加dx11
     */
    public void addDx11(){
        int index = startUpParams.indexOf("-dx12");
        if (index != -1){
            startUpParams.set(index,"-dx11");
        }else {
            startUpParams.add("-dx11");
        }
    }

    /**
     * 启动参数中添加dx12
     */
    public void addDx12(){
        int index = startUpParams.indexOf("-dx11");
        if (index != -1){
            startUpParams.set(index,"-dx12");
        }else {
            startUpParams.add("-dx12");
        }
    }

    public void replaceParam(String param1, String param2) {

    }


    @Override
    public void onViewAdded() {

    }

    @Override
    public void onViewRemoved() {
        checkGameLogOpen();
        Config.save();
    }
    /**
     * description: 检测游戏日志是否被关闭
     */
    private void checkGameLogOpen() {
//        CheckGameConfigTask task = new CheckGameConfigTask();
//        task.setOnSucceeded(workerStateEvent -> {
//            Boolean value = task.getValue();
//            if (!value) { //游戏日志可能被关闭了
//                Platform.runLater(() -> {
//                    NotificationManager.message(MessageInfo.success(LanguageManager.getString("ui.main.sync.message.log.close")));
//                });
//            }
//        });
//        Thread.startVirtualThread(task);
    }

    public boolean changeServer(GameClientType sourceType) {
        return false;
    }


    public GameClientType getGameClientType() {
        return gameClientType.get();
    }

    public SimpleObjectProperty<GameClientType> gameClientTypeProperty() {
        return gameClientType;
    }

    public void setGameClientType(GameClientType gameClientType) {
        this.gameClientType.set(gameClientType);
    }

    public String getGameDir() {
        return gameDir.get();
    }

    public SimpleStringProperty gameDirProperty() {
        return gameDir;
    }

    public boolean isSourceTypeDisabled01() {
        return sourceTypeDisabled01.get();
    }

    public SimpleBooleanProperty sourceTypeDisabled01Property() {
        return sourceTypeDisabled01;
    }

    public boolean isSourceTypeDisabled02() {
        return sourceTypeDisabled02.get();
    }

    public SimpleBooleanProperty sourceTypeDisabled02Property() {
        return sourceTypeDisabled02;
    }

    public boolean isSourceTypeDisabled03() {
        return sourceTypeDisabled03.get();
    }

    public SimpleBooleanProperty sourceTypeDisabled03Property() {
        return sourceTypeDisabled03;
    }

    public String getGameAppStartPath() {
        return gameAppStartPath.get();
    }

    public SimpleStringProperty gameAppStartPathProperty() {
        return gameAppStartPath;
    }

    public boolean isGameAppStartCustom() {
        return gameAppStartCustom.get();
    }

    public SimpleBooleanProperty gameAppStartCustomProperty() {
        return gameAppStartCustom;
    }

    public ObservableList<String> getStartUpParams() {
        return startUpParams;
    }




}