package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.base.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @description: 单实例 IPC 客户端。第二个实例检测到多开后调用 sendShow()，
 *               连接已运行实例的 IPC 服务端并发送 SHOW 指令，通知其显示主窗口。
 *               连接失败仅记日志，不影响第二个实例退出流程。
 * @author: Leck
 * @create: 2026-08-29
 */
public class SingleInstanceClient {
    private static final Logger LOG = LoggerFactory.getLogger(SingleInstanceClient.class);

    /** 连接超时（毫秒），避免卡住第二个实例 */
    private static final int CONNECT_TIMEOUT_MS = 1000;

    /** SHOW 指令：要求已运行实例显示主窗口 */
    private static final String CMD_SHOW = "SHOW";

    /**
     * @description: 向已运行实例发送 SHOW 指令，唤起其主窗口。
     * @param:
     * @return: void
     * @date:   2026/08/29
     */
    public static void sendShow() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(AppConstants.IPC_HOST, AppConstants.IPC_PORT),
                    CONNECT_TIMEOUT_MS);
            try (Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(CMD_SHOW + "\n");
                writer.flush();
            }
            LOG.info("已向已运行实例发送 SHOW 指令");
        } catch (IOException e) {
            LOG.warn("连接已运行实例 IPC 服务端失败，无法唤起主窗口：{}", e.getMessage());
        }
    }
}
