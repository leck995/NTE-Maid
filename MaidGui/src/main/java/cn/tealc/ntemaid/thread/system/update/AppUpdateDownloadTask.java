package cn.tealc.ntemaid.thread.system.update;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.teafx.utils.ResponseBody;
import cn.tealc.teafx.utils.message.MessageInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * @description: 下载新版本助手压缩包，且解压
 * @author: Leck
 * @create: 2024-12-22 20:27
 */
public class AppUpdateDownloadTask extends Task<ResponseBody<Boolean>> {
    private static final Logger LOG = LoggerFactory.getLogger(AppUpdateDownloadTask.class);

    private final File targetFile;
    private final Release release;
    private final String url;

    public AppUpdateDownloadTask(Release release,int urlIndex) {
        this.release = release;
        LOG.info(release.getMd5());
        String fileName = String.format("NTEMaid-%s.jar", release.getVersion());
        targetFile = new File("app/" + fileName);
        url = release.getUrls()[urlIndex];
        LOG.info("更新网站: {}", url);
    }

    @Override
    protected ResponseBody<Boolean> call() throws Exception {
        updateProgress(0, 1);
        if (targetFile.exists()) {
            String md5 = DigestUtils.md5Hex(new FileInputStream(targetFile));
            LOG.info("本地MD5:{},网络MD5:{}",md5,release.getMd5());
            if (md5.equals(release.getMd5())) {
                LOG.info("本地存在新版本文件，进行安装");
                updateAppClasspathAdvanced(targetFile.getName());
                return ResponseBody.create(200, "准备安装", true);
            } else {
                deleteFile();
            }
        }

        ResponseBody<Boolean> result = doDownload();
        return result;
    }

    private ResponseBody<Boolean> doDownload() {
        HttpClient client = AppInjector.getInstance(HttpClient.class);
        try {
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept-Encoding", "identity")
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                LOG.error("下载安装包失败,错误代码: {}", response.statusCode());
                return ResponseBody.create(-1, "下载安装包失败，状态码: " + response.statusCode(), false);
            }
            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            if (contentLength <= 0) {
                LOG.error("无法获取文件大小");
                return ResponseBody.create(201, "下载文件大小校验失败，请重新尝试", false);
            }
            updateTitle(String.format("%d M", contentLength / 1000 / 1000));
            try (InputStream inputStream = response.body();
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[65536];
                int bytesRead;
                long totalBytesRead = 0;
                long lastUpdate = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (isCancelled()) {
                        deleteFile();
                        return ResponseBody.create(-1, "下载已取消", false);
                    }
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (totalBytesRead - lastUpdate >= 524288) {
                        updateProgress(totalBytesRead, contentLength);
                        lastUpdate = totalBytesRead;
                    }
                }
                updateProgress(totalBytesRead, contentLength);
            }
            if (targetFile.length() != contentLength) {
                LOG.error("文件大小不匹配: expected={}, actual={}", contentLength, targetFile.length());
                return ResponseBody.create(201, "文件下载不完整", false);
            }
            if (checkMd5()) {
                LOG.info("MD5验证通过，开始安装");
                updateAppClasspathAdvanced(targetFile.getName());
                return ResponseBody.create(200, "准备安装", true);
            }
            return ResponseBody.create(201, "校验安装包失败", false);
        } catch (HttpTimeoutException e) {
            LOG.error("请求超时: {}", e.getMessage());
            return ResponseBody.create(408, "请求超时", false);
        } catch (Exception e) {
            LOG.error("发生错误: {}", e.getMessage());
            return ResponseBody.create(-1, "发生错误: " + e.getMessage(), false);
        }
    }

    private boolean checkMd5() {
        try (FileInputStream inputStream = new FileInputStream(targetFile)) {
            String md5 = DigestUtils.md5Hex(inputStream);
            return md5.equals(release.getMd5());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }


    public static void updateAppClasspathAdvanced(String jarName) throws IOException {
        Path path = Path.of("app", "NTEMaid.cfg");
        List<String> lines = Files.readAllLines(path);
        boolean modified = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().startsWith("app.classpath=")) {
                String newLine = "app.classpath=$APPDIR\\" + jarName;
                lines.set(i, newLine);
                modified = true;
                break;
            }
        }

        if (modified) {
            Files.write(path, lines);
            System.out.println("jar文件已更新: " + jarName);
        } else {
            System.err.println("错误: 未找到 app.classpath 配置项");
        }
    }


    private void deleteFile() {
        if (targetFile.exists()) {
            targetFile.delete();
        }
    }
}