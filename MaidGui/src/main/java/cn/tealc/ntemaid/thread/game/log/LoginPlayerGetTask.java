package cn.tealc.ntemaid.thread.game.log;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.model.game.Player;
import cn.tealc.ntemaid.service.system.GameServerService;
import cn.tealc.ntemaid.util.GameClientType;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从游戏日志中读取最近一次登录的角色信息。
 * 从文件末尾开始向上查找第一个包含 CHDGamePlayerMgr::setRoleInfo 的行，
 * 解析出 roleId 和 roleName，作为当前登录玩家（isOwner = true）。
 */
public class LoginPlayerGetTask extends Task<Player> {
    private static final Logger log = LoggerFactory.getLogger(LoginPlayerGetTask.class);

    private static final String MARKER = "CHDGamePlayerMgr::setRoleInfo";
    /** 游戏日志默认为 GBK 编码（国服与国际服一致） */
    private static final Charset LOG_CHARSET = Charset.forName("GBK");
    private static final Pattern ROLE_ID_PATTERN =
            Pattern.compile("\"roleId\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern ROLE_NAME_PATTERN =
            Pattern.compile("\"roleName\"\\s*:\\s*\"([^\"]*)\"");
    private static final int CHUNK_SIZE = 8192;

    private final String logPath;

    public LoginPlayerGetTask() {
        GameServerService serverService = AppInjector.getInstance(GameServerService.class);
        GameClientType serverType = serverService.detectServer();

        if (serverType == GameClientType.GLOBAL) {
            // 国际服：日志位于 NTEGlobal 目录
            logPath = Config.getSetting().getGameRootDir() + "/NTEGlobal/UserData/Log/NTEGlobalGame.log";
        } else {
            // 国服（含官服/B服）：日志位于 NTELauncher 目录
            logPath = Config.getSetting().getGameRootDir() + "/NTELauncher/UserData/Log/NTEGame.log";
        }
    }

    @Override
    protected Player call() throws Exception {
        File file = new File(logPath);
        if (!file.exists() || !file.isFile()) {
            log.warn("游戏日志文件不存在: {}", logPath);
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) {
                log.warn("游戏日志文件为空: {}", logPath);
                return null;
            }

            long pos = fileLength;
            ByteArrayOutputStream pending = new ByteArrayOutputStream();

            while (pos > 0) {
                if (isCancelled()) {
                    return null;
                }
                int readSize = (int) Math.min(CHUNK_SIZE, pos);
                pos -= readSize;
                raf.seek(pos);
                byte[] buffer = new byte[readSize];
                raf.readFully(buffer);

                // 从当前 chunk 末尾向前逐字节处理
                for (int i = buffer.length - 1; i >= 0; i--) {
                    byte b = buffer[i];
                    if (b == '\n') {
                        if (pending.size() > 0) {
                            Player player = parsePendingLine(pending);
                            if (player != null) {
                                return player;
                            }
                            pending.reset();
                        }
                    } else if (b != '\r') {
                        pending.write(b);
                    }
                }
            }

            // 处理文件开头可能未以换行结尾的最后一行
            if (pending.size() > 0) {
                return parsePendingLine(pending);
            }
        }

        log.info("日志中未找到登录角色信息");
        return null;
    }

    /**
     * 将待处理字节（按逆序收集）还原为正常行文本并尝试解析
     */
    private Player parsePendingLine(ByteArrayOutputStream pending) {
        byte[] bytes = pending.toByteArray();
        // 字节是逆序收集的，需要反转回原始顺序
        for (int i = 0, j = bytes.length - 1; i < j; i++, j--) {
            byte tmp = bytes[i];
            bytes[i] = bytes[j];
            bytes[j] = tmp;
        }
        String line = new String(bytes, LOG_CHARSET);
        if (line.contains(MARKER)) {
            return parsePlayer(line);
        }
        return null;
    }

    /**
     * 从包含 CHDGamePlayerMgr::setRoleInfo 的日志行中解析 roleId 和 roleName
     */
    private Player parsePlayer(String line) {
        Matcher idMatcher = ROLE_ID_PATTERN.matcher(line);
        Matcher nameMatcher = ROLE_NAME_PATTERN.matcher(line);
        if (idMatcher.find() && nameMatcher.find()) {
            try {
                long roleId = Long.parseLong(idMatcher.group(1));
                String roleName = nameMatcher.group(1);
                log.info("找到登录角色: id={}, name={}", roleId, roleName);
                return new Player(roleId, roleName, true);
            } catch (NumberFormatException e) {
                log.warn("roleId 解析失败: {}", idMatcher.group(1), e);
            }
        }
        return null;
    }
}
