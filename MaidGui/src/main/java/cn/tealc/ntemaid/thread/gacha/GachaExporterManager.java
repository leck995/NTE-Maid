package cn.tealc.ntemaid.thread.gacha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 抓包组件（gacha/nte-gacha-exporter-cli.exe）管理器。
 * <p>
 * 抓包组件来自上游开源项目 Anong0u0/NTE_Gacha_Exporter，其卡池/封包映射数据
 * 与游戏版本强相关：游戏更新新卡池（如 1.3 的残红、娜娜莉复刻、伊洛伊）后，
 * 旧版本组件无法识别新卡池导致抓取失败（issue #7）。
 * <p>
 * 本管理器在启动抓取前检测本地组件版本，低于要求版本时自动从上游
 * GitHub Releases 下载并替换，使老安装无需重新打包即可恢复抓取能力。
 */
public final class GachaExporterManager {
    private static final Logger LOG = LoggerFactory.getLogger(GachaExporterManager.class);

    /** 组件目录与文件名（相对应用工作目录，与发行包结构一致） */
    public static final String EXPORTER_DIR = "gacha";
    public static final String EXPORTER_EXE_NAME = "nte-gacha-exporter-cli.exe";

    /**
     * 最低要求的抓包组件版本。
     * v1.2.4 适配游戏 1.3：新增残红（ForkLottery_Zhenhong）、伊洛伊卡池与
     * 娜娜莉复刻（monopoly_limited_Nanali）等卡池映射。
     */
    public static final String REQUIRED_VERSION = "1.2.4";

    /** 上游发布包下载地址模板 */
    private static final String DOWNLOAD_URL_TEMPLATE =
            "https://github.com/Anong0u0/NTE_Gacha_Exporter/releases/download/v%s/nte-gacha-exporter-%s.zip";

    /** 版本探测超时（秒） */
    private static final int VERSION_TIMEOUT_S = 10;

    /** 下载/解压结果的最小合法体积（1MB），用于防止拿到错误页面 */
    private static final long MIN_EXE_SIZE = 1024L * 1024L;

    private GachaExporterManager() {
    }

    /** 获取本地抓包组件文件 */
    public static File getExporterFile() {
        return new File(EXPORTER_DIR, EXPORTER_EXE_NAME);
    }

    /** 上游发布包下载地址 */
    public static String getDownloadUrl() {
        return String.format(DOWNLOAD_URL_TEMPLATE, REQUIRED_VERSION, REQUIRED_VERSION);
    }

    /**
     * 确保抓包组件存在且版本满足要求：缺失或版本过低时自动下载替换。
     * <p>
     * 更新失败时若本地已有组件则继续使用本地版本（不阻断抓取）；
     * 本地无组件且下载失败则抛出异常，异常信息包含手动下载指引。
     *
     * @param progress 进度回调（可为 null），用于向用户展示状态信息
     * @throws IOException 组件缺失且无法下载时抛出
     */
    public static void ensureExporter(Consumer<String> progress) throws IOException {
        File exe = getExporterFile();
        File dir = exe.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        boolean exists = exe.exists();
        String current = exists ? readVersion(exe) : null;
        if (exists && current != null && !isVersionBelow(current, REQUIRED_VERSION)) {
            LOG.debug("抓包组件版本满足要求: {}", current);
            return;
        }
        if (exists && current == null) {
            LOG.warn("无法读取抓包组件版本，将尝试更新: {}", exe.getAbsolutePath());
        }

        try {
            notify(progress, "正在下载抓取组件 v" + REQUIRED_VERSION + " ...");
            downloadAndInstall(exe, progress);
            String updated = readVersion(exe);
            LOG.info("抓包组件已更新: v{}", updated);
            notify(progress, "抓取组件已更新到 v" + (updated != null ? updated : REQUIRED_VERSION));
        } catch (Exception e) {
            LOG.error("抓包组件更新失败", e);
            if (!exists) {
                throw new IOException("抓取组件下载失败：" + e.getMessage()
                        + "。请手动下载 " + getDownloadUrl()
                        + "，解压后将 " + EXPORTER_EXE_NAME + " 放到程序的 "
                        + EXPORTER_DIR + " 目录中后重试", e);
            }
            notify(progress, "抓取组件更新失败，将使用本地版本（v" + (current != null ? current : "未知") + "）");
        }
    }

    /**
     * 运行 {@code exe --version} 读取组件版本号。
     *
     * @return 版本号字符串（如 "1.2.4"），无法获取时返回 null
     */
    static String readVersion(File exe) {
        try {
            Process process = new ProcessBuilder(exe.getAbsolutePath(), "--version")
                    .redirectErrorStream(true)
                    .start();
            // 组件应立即输出并退出，先限时等待退出再读取全部输出，避免读取阻塞
            if (!process.waitFor(VERSION_TIMEOUT_S, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return output.lines().findFirst().map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
        } catch (Exception e) {
            LOG.warn("读取抓包组件版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 版本比较：version 是否低于 required（按 x.y.z 逐段数值比较）。
     * version 无法解析时视为低于要求。
     */
    static boolean isVersionBelow(String version, String required) {
        int[] v = parseVersion(version);
        int[] r = parseVersion(required);
        if (v == null) {
            return true;
        }
        for (int i = 0; i < Math.max(v.length, r.length); i++) {
            int a = i < v.length ? v[i] : 0;
            int b = i < r.length ? r[i] : 0;
            if (a != b) {
                return a < b;
            }
        }
        return false;
    }

    /** 解析形如 1.2.4 的版本号；无法解析返回 null */
    private static int[] parseVersion(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }
        String cleaned = version.trim().split("[-+ ]")[0];
        String[] parts = cleaned.split("\\.");
        if (parts.length == 0) {
            return null;
        }
        int[] result = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }

    /** 下载上游发布包，解出 CLI 组件并替换本地文件 */
    private static void downloadAndInstall(File exe, Consumer<String> progress) throws IOException {
        Path zipFile = Files.createTempFile("nte-gacha-exporter-", ".zip");
        try {
            notify(progress, "正在下载抓取组件（约 11MB，视网络情况可能需要数分钟）...");
            downloadTo(getDownloadUrl(), zipFile);
            Path extracted = extractCliExe(zipFile);
            try {
                Files.move(extracted, exe.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                // 目标文件被占用（例如抓取正在进行）等情况：降级为普通替换
                try {
                    Files.move(extracted, exe.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new IOException("无法替换抓取组件（若正在抓取请先结束后重试）: " + e.getMessage(), e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("下载被中断", e);
        } finally {
            Files.deleteIfExists(zipFile);
        }
    }

    /** 下载 URL 内容到本地文件，并做基本的体积校验 */
    private static void downloadTo(String url, Path target) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("User-Agent", "NTEMaid")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " - " + url);
            }
            try (InputStream is = response.body();
                 OutputStream os = Files.newOutputStream(target)) {
                is.transferTo(os);
            }
        }
        if (Files.size(target) < MIN_EXE_SIZE) {
            throw new IOException("下载内容异常（文件过小）");
        }
    }

    /** 从发布包 zip 中解出 CLI 组件到临时文件 */
    private static Path extractCliExe(Path zipFile) throws IOException {
        Path out = Files.createTempFile("nte-gacha-exporter-cli-", ".exe");
        boolean found = false;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals(EXPORTER_EXE_NAME) || name.endsWith("/" + EXPORTER_EXE_NAME)) {
                    try (OutputStream os = Files.newOutputStream(out)) {
                        zis.transferTo(os);
                    }
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IOException("发布包中未找到 " + EXPORTER_EXE_NAME);
        }
        if (Files.size(out) < MIN_EXE_SIZE) {
            throw new IOException("解压结果异常（文件过小）");
        }
        return out;
    }

    private static void notify(Consumer<String> progress, String message) {
        if (progress != null) {
            progress.accept(message);
        }
    }
}
