package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.service.system.GameServerService;
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
        if (Config.getSetting().getGameRootDir() != null) {
            File dir = new File(Config.getSetting().getGameRootDir());
            if (!dir.exists()) {
                return Optional.empty();
            }
            return Optional.of(dir);
        }
        return Optional.empty();
    }

    public static Optional<File> getGameEngineIni() {
        String localAppData = System.getenv("LOCALAPPDATA");
        String savedDir = AppInjector.getInstance(GameServerService.class).getEngineSavedDirName();
        Path logPath = Paths.get(localAppData, "HT", savedDir, "Config", "Windows","Engine.ini");
        if (Files.exists(logPath)){
            return Optional.of(logPath.toFile());
        }
        return Optional.empty();
    }

    public static Optional<File> getGameScreenShoot() {
        String dir = Config.getSetting().getGameRootDir();
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
        String savedDir = AppInjector.getInstance(GameServerService.class).getEngineSavedDirName();
        Path logPath = Paths.get(localAppData, "HT", savedDir, "Screenshots", "Windows");
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
        String dir = Config.getSetting().getGameRootDir();
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
        String dir = Config.getSetting().getGameRootDir();
        File file = null;
        if (dir != null) {
            file = new File(dir + File.separator + "Client/Saved/Logs/Client.log");
        }
        return file;
    }
}