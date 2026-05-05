package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.base.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @description: 负责获取部分游戏文件
 * @author: Leck
 * @create: 2024-10-19 01:35
 */
public class GameResourcesManager {
    private static final Logger LOG = LoggerFactory.getLogger(GameResourcesManager.class);

    public static Optional<File> getGameDir() {
        if (Config.setting.getGameRootDir() != null) {
            File dir = new File(Config.setting.getGameRootDir());
            if (!dir.exists()) {
                return Optional.empty();
            }
            return Optional.of(dir);
        }
        return Optional.empty();
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

    public static Optional<File> getGameScreenShoot() {
        String dir = Config.setting.getGameRootDir();
        if (dir != null) {
            Path path = Paths.get(dir, "Client", "WindowsNoEditor", "Selfie");
            if (Files.exists(path)) {
                try {
                    File[] files = path.toFile().listFiles(File::isDirectory);

                    if (files != null && files.length == 1 && files[0].isDirectory()) {
                        // 如果只有一个子目录，返回该子目录
                        File file = new File(files[0],"ScreenShots");
                        return Optional.of(file);
                    } else {
                        // 否则返回原路径
                        return Optional.of(path.toFile());
                    }
                } catch (SecurityException e) {
                    // 处理权限异常
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
    public static Optional<File> getGameScreenShoot2() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path logPath = Paths.get(localAppData, "HT", "Saved", "Screenshots", "Windows");
        if (Files.exists(logPath)){
            return Optional.of(logPath.toFile());
        }
        return Optional.empty();
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