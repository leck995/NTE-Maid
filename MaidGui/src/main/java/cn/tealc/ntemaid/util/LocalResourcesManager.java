package cn.tealc.ntemaid.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class LocalResourcesManager {
    private static final Logger LOG = LoggerFactory.getLogger(LocalResourcesManager.class);
    public static final String BUFFER_DIR_TEMPLATE = "resources/cache/%s";
    public static final String HOME_BG_TEMPLATE = "resources/image/bg/%s";


    private static Image imageBuffer(String url) {
        File file = new File(String.format(BUFFER_DIR_TEMPLATE, getName(url)));
        if (file.exists()) { //有缓存，获取
            return new Image(file.toURI().toString(), true);
        } else { //无缓存，获取并保存
            LOG.debug("{}无缓存，获取并保存", file);
            Image image = new Image(url, true);
            saveImage(image, file);
            return image;
        }
    }

    private static Image imageBuffer(String url, double width, double height, boolean preserveRatio, boolean smooth) {
        File file = new File(String.format(BUFFER_DIR_TEMPLATE, getName(url)));
        if (file.exists()) { //有缓存，获取
            return new Image(file.toURI().toString(), width, height, preserveRatio, smooth, true);
        } else { //无缓存，获取并保存
            LOG.debug("{}无缓存，获取并保存", file);
            Image image = new Image(url, true);
            saveImage(image, file);
            return image;
        }
    }

    /**
     * @return javafx.scene.image.Image
     * @description:
     * @param: url
     * @date: 2024/8/7
     */
    public static Image getHomeBg(String filename) {
        File file = new File(String.format(HOME_BG_TEMPLATE, filename));
        if (file.exists()) {
            return new Image(file.toURI().toString(), 2560, 1440, true, true, false);
        } else {
            return null;
        }
    }

    public static String setHomeBg(File sourceFile, String oldFileName) throws IOException {
        String suffix = getSuffix(sourceFile.getName());
        String newName = System.currentTimeMillis() + "." + suffix;
        File newFile = new File(String.format(HOME_BG_TEMPLATE, newName));
        Files.createDirectories(newFile.getParentFile().toPath());
        Files.copy(sourceFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if (oldFileName != null && !oldFileName.isEmpty()) {
            File oldFile = new File(String.format(HOME_BG_TEMPLATE, oldFileName));
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                LOG.info("旧背景删除: {}", deleted);
            }
        }
        return newName;
    }

    public static String getName(String url) {
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }

    public static String getSuffix(String url) {
        int index = url.lastIndexOf(".");
        return url.substring(index + 1);
    }


    /**
     * @return void
     * @description: 对不存在缓存的图像资源进行缓存
     * @param: image
     * @param: file
     * @date: 2024/10/22
     */
    private static void saveImage(Image image, File file) {
        Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    if (image.getProgress() == 1) {
                        break;
                    } else {
                        Thread.sleep(100);
                    }
                }
                BufferedImage read = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(read, getSuffix(file.getName()), file);
            } catch (IOException | InterruptedException e) {
                LOG.error("保存图片缓存出错", e);
            }
        });
    }
}