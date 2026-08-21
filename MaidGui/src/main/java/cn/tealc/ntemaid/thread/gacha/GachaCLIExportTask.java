package cn.tealc.ntemaid.thread.gacha;

import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.service.ConfigService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * 启动时校验/替换 gacha CLI 组件（nte-gacha-exporter-cli.exe）。
 * <p>
 * 以 jar 内打包的 exe 为基准：启动后读取打包的参考 SHA1（classpath 资源
 * {@code /gacha/sha1}），计算本地 {@code gacha/nte-gacha-exporter-cli.exe} 的
 * 完整 SHA1 进行比对，不一致则用 jar 内打包 exe 覆盖本地文件。
 *
 * <p><b>一次性执行</b>：覆盖成功后在 config 表写入标志位 {@link #CONFIG_KEY_DONE}。
 * 只要该标志存在，后续启动直接跳过本任务，避免覆盖用户后续手动替换/联网升级的更新版本。
 * （即：本任务只在"升级到带本任务的版本后"执行一次，把本地校准为打包版本。）
 *
 * <p>本任务与 {@link GachaExporterManager} 职责不冲突：后者在抓取前仍会做版本
 * 检测并可能联网升级到更新版本。
 */
public class GachaCLIExportTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(GachaCLIExportTask.class);

    /** jar 内资源路径 */
    private static final String RESOURCE_SHA1_PATH = "/gacha/sha1";
    private static final String RESOURCE_EXE_PATH = "/gacha/nte-gacha-exporter-cli.exe";

    /** config 表标志位：存在即表示本任务已成功执行过，后续启动跳过 */
    private static final String CONFIG_KEY_DONE = "gacha_cli_export_done";

    @Override
    public void run() {
        try {
            // 一次性：已执行过则跳过，不再覆盖本地（保护用户后续手动替换/联网升级的版本）
            ConfigService configService = AppInjector.getInstance(ConfigService.class);
            if (configService.getConfig(CONFIG_KEY_DONE).isPresent()) {
                LOG.debug("gacha CLI 组件校准任务已执行过，跳过");
                return;
            }

            String expected = readExpectedSha1();
            if (expected == null) {
                LOG.warn("未读取到打包 sha1，无法校验 gacha CLI 组件，跳过");
                return;
            }
            if (ensureLocalExporter(expected)) {
                // 覆盖成功（或本就一致）后写入标志位，后续启动不再执行
                configService.setConfig(CONFIG_KEY_DONE, true);
                LOG.info("gacha CLI 组件校准完成，已置位 {}", CONFIG_KEY_DONE);
            }
        } catch (Exception e) {
            // 启动期任务：任何异常只记日志，绝不阻断 JavaFX 启动
            LOG.error("校验/替换 gacha CLI 组件失败", e);
        }
    }

    /**
     * 读取 jar 内 {@code /gacha/sha1} 文件的参考 SHA1。
     *
     * @return 参考 SHA1（小写十六进制），读取失败返回 null
     */
    private String readExpectedSha1() {
        try (InputStream is = FXResourcesLoader.loadStream(RESOURCE_SHA1_PATH)) {
            if (is == null) {
                LOG.warn("classpath 中不存在 {}", RESOURCE_SHA1_PATH);
                return null;
            }
            String sha1 = new String(is.readAllBytes()).trim();
            return sha1.isEmpty() ? null : sha1;
        } catch (IOException e) {
            LOG.warn("读取打包 sha1 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验本地 exe 与打包参考是否一致，不一致则用 jar 内 exe 覆盖。
     *
     * @param expectedSha1 参考 SHA1（来自 jar 内 sha1 文件）
     * @return true 表示已校准（本就一致或覆盖成功），可置位标志；false 表示未完成不置位
     */
    private boolean ensureLocalExporter(String expectedSha1) throws IOException {
        Path local = GachaExporterManager.getExporterFile().toPath();

        if (!Files.exists(local)) {
            LOG.info("本地 gacha CLI 组件不存在，从 jar 内释放");
            return replaceFromJar(local, expectedSha1);
        }

        String actual;
        try (InputStream is = Files.newInputStream(local)) {
            actual = DigestUtils.sha1Hex(is);
        }

        if (expectedSha1.equalsIgnoreCase(actual)) {
            LOG.debug("gacha CLI 组件 SHA1 校验通过({})，跳过替换", actual);
            return true;
        }

        LOG.info("本地组件 SHA1({})与打包参考({})不一致，将覆盖", actual, expectedSha1);
        return replaceFromJar(local, expectedSha1);
    }

    /**
     * 从 jar 内读取打包 exe，覆盖本地文件。覆盖完成后做一次完整 SHA1 校验。
     *
     * @return true 表示覆盖成功且校验通过；false 表示覆盖后校验仍不符
     */
    private boolean replaceFromJar(Path local, String expectedSha1) throws IOException {
        Path parent = local.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tmp = Files.createTempFile("nte-gacha-exporter-cli-", ".exe");
        try {
            try (InputStream is = FXResourcesLoader.loadStream(RESOURCE_EXE_PATH)) {
                Objects.requireNonNull(is, "jar 内不存在 " + RESOURCE_EXE_PATH);
                Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            }

            // 原子替换，失败降级为普通替换（仿 GachaExporterManager.downloadAndInstall）
            try {
                Files.move(tmp, local, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                LOG.warn("原子替换失败，降级为普通替换: {}", atomicFailure.getMessage());
                Files.move(tmp, local, StandardCopyOption.REPLACE_EXISTING);
            }

            // 覆盖后校验：确认写入的文件与参考 SHA1 一致
            String actual;
            try (InputStream is = Files.newInputStream(local)) {
                actual = DigestUtils.sha1Hex(is);
            }
            if (expectedSha1.equalsIgnoreCase(actual)) {
                LOG.info("gacha CLI 组件已替换完成，SHA1={}", actual);
                return true;
            } else {
                // 写入完成但校验未通过：不回滚、不抛出，仅告警（不阻断启动）
                LOG.warn("gacha CLI 组件已覆盖，但写入后 SHA1({})与参考({})不符", actual, expectedSha1);
                return false;
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}

