package cn.tealc.ntemaid.ui.game.manage;


import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.util.GameResourcesManager;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.ntemaid.util.GameClientType;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;

/**
 * @description:
 * @author: Leck
 * @create: 2025-02-10 19:18
 */
public class GameSettingParentViewModel implements ViewModel {
    private SimpleObjectProperty<GameClientType> gameRootDirSource = new SimpleObjectProperty<>();
    private SimpleStringProperty gameRootDir=new SimpleStringProperty();

    public GameSettingParentViewModel() {
        gameRootDirSource.bindBidirectional(Config.setting.gameRootDirSourceProperty());
        gameRootDir.bindBidirectional(Config.setting.gameRootDirProperty());
    }


    /**
     * 判断游戏是否已安装
     * @return boolean
     */
    public boolean installed(){
        File gameDir = GameResourcesManager.getGameDir();
        if (gameDir == null)
            return false;

        File startApp = new File(gameDir.getAbsolutePath() + File.separator + "Wuthering Waves.exe");
        return startApp.exists();
    }



    public void setGameDir(File gameDir) {
        File startApp = new File(gameDir.getAbsolutePath() + File.separator + "Wuthering Waves.exe");
        if (startApp.exists()) {
            gameRootDir.set(gameDir.getAbsolutePath());
        }else {
            MvvmFX.getNotificationCenter().publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.setting.message.01")));
        }
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
}