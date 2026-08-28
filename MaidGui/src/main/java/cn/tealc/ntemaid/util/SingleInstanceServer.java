package cn.tealc.ntemaid.util;

import cn.tealc.ntemaid.base.AppConstants;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @description: 单实例 IPC 服务端。第一个实例启动时开启本地 ServerSocket 监听，
 *               当第二个实例检测到多开后连接并发送 SHOW 指令时，本服务端接收并
 *               通过 NotificationManager 发布 APP_SHOW 通知，由主窗口订阅回调
 *               将窗口从托盘/最小化状态恢复显示。
 * @author: Leck
 * @create: 2026-08-29
 */
@Singleton
public class SingleInstanceServer {
    private static final Logger LOG = LoggerFactory.getLogger(SingleInstanceServer.class);

    /** SHOW 指令：要求已运行实例显示主窗口 */
    private static final String CMD_SHOW = "SHOW";

    private ServerSocket serverSocket;
    private volatile boolean stopped = false;

    /**
     * @description: 启动 IPC 服务端，在虚拟线程中循环 accept 连接。
     *               端口被占用时仅记日志不抛异常，降级为无唤起能力（文件锁检测仍有效）。
     * @param:
     * @return: void
     * @date:   2026/08/29
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(AppConstants.IPC_PORT, 0,
                    java.net.InetAddress.getByName(AppConstants.IPC_HOST));
        } catch (IOException e) {
            LOG.warn("IPC 服务端启动失败（端口 {} 可能被占用），多开唤起能力将不可用：{}",
                    AppConstants.IPC_PORT, e.getMessage());
            return;
        }
        Thread.startVirtualThread(this::acceptLoop);
        LOG.info("IPC 服务端已启动，监听 {}:{}", AppConstants.IPC_HOST, AppConstants.IPC_PORT);
    }

    /**
     * @description: accept 循环，每收到一个连接读取一行指令并处理。
     * @date:   2026/08/29
     */
    private void acceptLoop() {
        while (!stopped) {
            try (Socket socket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                if (CMD_SHOW.equals(line)) {
                    LOG.info("收到多开实例的 SHOW 指令，唤起主窗口");
                    NotificationManager.publish(NotificationKey.APP_SHOW);
                }
            } catch (IOException e) {
                if (!stopped) {
                    LOG.error("IPC 服务端 accept 异常", e);
                }
            }
        }
    }

    /**
     * @description: 停止 IPC 服务端，关闭 ServerSocket 使 accept 循环退出。
     * @param:
     * @return: void
     * @date:   2026/08/29
     */
    public void stop() {
        stopped = true;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOG.error("关闭 IPC 服务端异常", e);
            }
        }
    }
}
