package cn.tealc.ntemaid.thread.system.update;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DeleteOldAppVersionTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(DeleteOldAppVersionTask.class.getName());
    private static final String APP_DIR = "app";
    private static final String CONFIG_FILE = "NTEMaid.cfg";
    private static final String CLASSPATH_PREFIX = "app.classpath=";
    private static final String APPDIR_PREFIX = "$APPDIR\\";

    @Override
    public void run() {
        Path appPath = Path.of(APP_DIR);
        if (!Files.exists(appPath)) {
            return;
        }
        getNewJar().ifPresent(newJarName -> deleteOldJarFiles(appPath, newJarName));
    }

    /**
     * 获取新版本的JAR文件名
     * @return 新JAR文件名（如果存在）
     */
    public Optional<String> getNewJar() {
        Path configPath = Path.of(APP_DIR, CONFIG_FILE);

        if (!Files.exists(configPath)) {
            LOGGER.warning("配置文件不存在: " + configPath);
            return Optional.empty();
        }

        try (Stream<String> lines = Files.lines(configPath)) {
            return lines
                    .map(String::trim)
                    .filter(line -> line.startsWith(CLASSPATH_PREFIX))
                    .findFirst()
                    .map(line -> line.replace(CLASSPATH_PREFIX + APPDIR_PREFIX, ""));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "读取配置文件失败: " + configPath, e);
            return Optional.empty();
        }
    }

    /**
     * 删除旧的JAR文件（保留新版本）
     * @param appPath 应用目录路径
     * @param newJarName 新JAR文件名
     */
    private void deleteOldJarFiles(Path appPath, String newJarName) {
        try (Stream<Path> files = Files.list(appPath)) {
            files.filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.endsWith(".jar") && !fileName.equals(newJarName);
                    })

                    .forEach(this::deleteFileSafely);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "遍历目录失败: " + appPath, e);
        }
    }

    /**
     * 安全删除文件
     * @param path 要删除的文件路径
     */
    private void deleteFileSafely(Path path) {
        try {
            if (Files.deleteIfExists(path)) {
                LOGGER.info("已删除旧版本: " + path.getFileName());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "删除文件失败: " + path, e);
        }
    }
}