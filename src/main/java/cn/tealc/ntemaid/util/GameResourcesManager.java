package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.base.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 * @description: 负责获取部分游戏文件
 * @author: Leck
 * @create: 2024-10-19 01:35
 */
public class GameResourcesManager {
    private static final Logger LOG = LoggerFactory.getLogger(GameResourcesManager.class);

    public static File getGameDir() {
        if (Config.setting.getGameRootDir() != null) {
            File dir = new File(Config.setting.getGameRootDir());
            if (!dir.exists()) {
                return null;
            }
            return dir;
        }
        return null;
    }


    public static File getGameExeBase() {
        String dir = Config.setting.getGameRootDir();
        File exe = null;
        if (dir != null) {
            exe = new File(dir + File.separator + "NTELauncher.exe");
            if (!exe.exists()) {
                return null;
            }
        }
        return exe;
    }

    public static File getGameEngineIni() {
        String dir = Config.setting.getGameRootDir();
        File exe = null;
        if (dir != null) {
            exe = new File(dir + File.separator + "Client/Saved/Config/WindowsNoEditor/Engine.ini");
            if (!exe.exists()) {
                return null;
            }
        }
        return exe;
    }


    public static File getGameScreenShoot() {
        String dir = Config.setting.getGameRootDir();
        File exe = null;
        if (dir != null) {
            exe = new File(Config.setting.getGameRootDir() + File.separator + "Client/Saved/ScreenShot");
            if (!exe.exists()) {
                return null;
            }
        }
        return exe;
    }

    /**
     * @return java.io.File
     * @description: 获取游戏日志文件夹
     * @param:
     * @date: 2024/11/13
     */
    public static File getGameLogDir() {
        String dir = Config.setting.getGameRootDir();
        File exe = null;
        if (dir != null) {
            exe = new File(dir + File.separator + "Client/Saved/Logs");
            if (!exe.exists()) {
                return null;
            }
        }
        return exe;
    }

    public static File getGameLogFile() {
        String dir = Config.setting.getGameRootDir();
        File file = null;
        if (dir != null) {
            file = new File(dir + File.separator + "Client/Saved/Logs/Client.log");
        }
        return file;
    }
}