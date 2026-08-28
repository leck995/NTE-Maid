package cn.tealc.ntemaid.service.system;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.util.GameClientType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏服务器类型判断服务。
 * <p>
 * 通过读取游戏安装目录下的 Config.ini 文件，判断当前游戏属于哪个服务器：
 * <ul>
 *   <li>国际服：{gameRootDir}/NTEGlobal/Config/Config.ini</li>
 *   <li>CN 官服：{gameRootDir}/NTELauncher/Config/Config.ini，[UPDATE_CONFIG].Branch=publish_ob</li>
 *   <li>CN B 服：{gameRootDir}/NTELauncher/Config/Config.ini，[UPDATE_CONFIG].Branch=publish_ob_B</li>
 * </ul>
 *
 * @author Leck
 */
@Singleton
public class GameServerService {
    private static final Logger log = LoggerFactory.getLogger(GameServerService.class);

    /** 国际服配置文件相对路径 */
    private static final Path GLOBAL_CONFIG = Paths.get("NTEGlobal", "Config", "Config.ini");
    /** CN 服配置文件相对路径 */
    private static final Path CN_CONFIG = Paths.get("NTELauncher", "Config", "Config.ini");

    /** CN B 服的 Branch 标识 */
    private static final String BRANCH_BILIBILI = "publish_ob_B";

    /** 国际服启动器文件名 */
    private static final String GLOBAL_LAUNCHER = "NTEGlobalLauncher.exe";
    /** 国服启动器文件名 */
    private static final String CN_LAUNCHER = "NTELauncher.exe";
    /** 国际服游戏进程名 */
    private static final String GLOBAL_GAME_PROCESS = "NTEGlobalGame.exe";
    /** 国服游戏进程名 */
    private static final String CN_GAME_PROCESS = "NTEGame.exe";
    /** 国际服引擎层 Saved 目录名 */
    private static final String GLOBAL_SAVED_DIR = "Saved_Global";
    /** 国服引擎层 Saved 目录名 */
    private static final String CN_SAVED_DIR = "Saved";

    @Inject
    public GameServerService() {}

    /**
     * 获取引擎层 Saved 目录名。
     *
     * @return 国际服返回 Saved_Global，国服返回 Saved
     */
    public String getEngineSavedDirName() {
        return detectServer() == GameClientType.GLOBAL ? GLOBAL_SAVED_DIR : CN_SAVED_DIR;
    }

    /**
     * 获取当前服务器对应的启动器文件名。
     *
     * @return 国际服返回 NTEGlobalLauncher.exe，国服返回 NTELauncher.exe
     */
    public String getLauncherFileName() {
        return detectServer() == GameClientType.GLOBAL ? GLOBAL_LAUNCHER : CN_LAUNCHER;
    }

    /**
     * 获取当前服务器对应的游戏进程名。
     *
     * @return 国际服返回 NTEGlobalGame.exe，国服返回 NTEGame.exe
     */
    public String getGameProcessName() {
        return detectServer() == GameClientType.GLOBAL ? GLOBAL_GAME_PROCESS : CN_GAME_PROCESS;
    }

    /**
     * 获取游戏启动器可执行文件（游戏根目录 + 启动器文件名）。
     *
     * @return 启动器文件，若游戏目录未配置或文件不存在则返回 null
     */
    public File getGameExeFile() {
        String dir = Config.getSetting().getGameRootDir();
        if (dir == null || dir.isBlank()) {
            return null;
        }
        File exe = new File(dir, getLauncherFileName());
        return exe.exists() ? exe : null;
    }

    /**
     * 根据游戏安装目录下的 Config.ini 判断当前游戏服务器类型。
     * <p>
     * 判定流程：
     * <ol>
     *   <li>若 NTEGlobal/Config/Config.ini 存在，判定为 {@link GameClientType#GLOBAL}</li>
     *   <li>否则读取 NTELauncher/Config/Config.ini 的 [UPDATE_CONFIG].Branch：
     *     <ul>
     *       <li>publish_ob_B → {@link GameClientType#BILIBILI}</li>
     *       <li>其余（含 publish_ob）→ {@link GameClientType#DEFAULT}</li>
     *     </ul>
     *   </li>
     *   <li>目录为空或文件不存在 → {@link GameClientType#DEFAULT}（兜底）</li>
     * </ol>
     *
     * @return 当前游戏服务器类型
     */
    public GameClientType detectServer() {
        String root = Config.getSetting().getGameRootDir();
        if (root == null || root.isBlank()) {
            log.warn("游戏安装目录未配置，按默认服处理");
            return GameClientType.DEFAULT;
        }

        Path gameRoot = Paths.get(root);

        // 1. 先查国际服：NTEGlobal 目录存在即判定
        Path globalIni = gameRoot.resolve(GLOBAL_CONFIG);
        if (Files.isReadable(globalIni)) {
            log.info("检测到国际服配置文件: {}", globalIni);
            return GameClientType.GLOBAL;
        }

        // 2. 查 CN 服：NTELauncher/Config/Config.ini，按 Branch 区分官服/B服
        Path cnIni = gameRoot.resolve(CN_CONFIG);
        if (!Files.isReadable(cnIni)) {
            log.warn("未找到游戏配置文件，按默认服处理: {}", cnIni);
            return GameClientType.DEFAULT;
        }

        Map<String, String> config = parseIni(cnIni);
        String branch = config.get("UPDATE_CONFIG.Branch");
        if (BRANCH_BILIBILI.equals(branch)) {
            log.info("检测到 CN B 服: Branch={}", branch);
            return GameClientType.BILIBILI;
        }
        log.info("检测到 CN 官服: Branch={}", branch);
        return GameClientType.DEFAULT;
    }

    /**
     * 轻量 INI 解析：遍历行，遇 [section] 记当前段名，遇 key=value 存为 "section.key"。
     * 不引入第三方依赖。注释行（以 ; 或 # 开头）与空行跳过。
     *
     * @param iniPath 配置文件路径
     * @return 以 "section.key" 为键的有序映射
     */
    private Map<String, String> parseIni(Path iniPath) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(iniPath, StandardCharsets.UTF_8);
            String currentSection = "";
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith(";") || trimmed.startsWith("#")) {
                    continue;
                }
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    currentSection = trimmed.substring(1, trimmed.length() - 1);
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                result.put(currentSection + "." + key, value);
            }
        } catch (IOException e) {
            log.error("解析配置文件失败: {}", iniPath, e);
        }
        return result;
    }
}
