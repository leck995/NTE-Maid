package cn.tealc.ntemaid.thread.system.update;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.system.realease.Release;
import cn.tealc.teafx.utils.ResponseBody;
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
    private static final int MAX_RETRIES = 3;

    private final File saveFile;
    private final Release release;
    private String url;

    public AppUpdateDownloadTask(Release release) {
        this.release = release;
        LOG.info(release.getMd5());
        String fileName = String.format("NTEMaid-%s.jar", release.getVersion());
        saveFile = new File("app/" + fileName);
        if (release.getUrls().length == 1) {
            url = release.getUrls()[0];
        } else if (release.getUrls().length > 1) {
            if (Config.getSetting().getResourceSource() == 0) {
                url = release.getUrls()[0];
            } else {
                url = release.getUrls()[1];
            }
        }
    }

    @Override
    protected ResponseBody<Boolean> call() throws Exception {
        updateProgress(0, 1);
        if (saveFile.exists()) {
            String md5 = DigestUtils.md5Hex(new FileInputStream(saveFile));
            LOG.info("本地MD5:{},网络MD5:{}",md5,release.getMd5());

            if (md5.equals(release.getMd5())) {
                LOG.info("本地存在新版本文件，进行安装");
                updateAppClasspathAdvanced(saveFile.getName());
                return ResponseBody.create(200, "准备安装", true);
            } else {
                deleteZip();
            }
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            LOG.info("第 {} 次下载尝试", attempt);
            ResponseBody<Boolean> result = doDownload();
            if (result.getCode() == 200) {
                return result;
            }
            LOG.warn("第 {} 次下载失败: {}", attempt, result.getMsg());
            deleteZip();
            if (attempt == MAX_RETRIES) {
                return ResponseBody.create(-1, "下载失败，已重试 " + MAX_RETRIES + " 次: " + result.getMsg(), false);
            }
        }
        return ResponseBody.create(-1, "下载失败", false);
    }

    private ResponseBody<Boolean> doDownload() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5)).build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                LOG.error("下载安装包失败,错误代码: {}", response.statusCode());
                return ResponseBody.create(-1, "下载安装包失败，状态码: " + response.statusCode(), false);
            }
            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
            if (contentLength <= 0) {
                LOG.error("无法获取文件大小");
                return ResponseBody.create(201, "无法获取文件大小", false);
            }
            updateTitle(String.format("%d M", contentLength / 1000 / 1000));
            try (InputStream inputStream = response.body();
                 FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    updateProgress(totalBytesRead, contentLength);
                }
            }
            if (saveFile.length() != contentLength) {
                LOG.error("文件大小不匹配: expected={}, actual={}", contentLength, saveFile.length());
                return ResponseBody.create(201, "文件下载不完整", false);
            }
            if (checkMd5()) {
                LOG.error("MD5验证通过，开始安装");
                updateAppClasspathAdvanced(saveFile.getName());
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
        try (FileInputStream inputStream = new FileInputStream(saveFile)) {
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


    private void deleteZip() {
        if (saveFile.exists()) {
            saveFile.delete();
        }
    }
}