package cn.tealc.ntemaid.jna;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import cn.tealc.ntemaid.snapshot.WindowCapture;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GlobalKeyListener implements NativeKeyListener {

    private static final String GAME_NAME = "异环  ";
    private static final Logger log = LoggerFactory.getLogger(GlobalKeyListener.class);

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!Config.getSetting().isMusicEnable()) {
            return;
        }
        boolean isGameActive = getForegroundWindowTitle()
                .map(t -> t.equals(GAME_NAME))
                .orElse(false);
        if (!isGameActive) {
            return;
        }


        int keyCode = e.getKeyCode();
        System.out.println(keyCode);
        BaseAudioPlayer player = MusicPlayerClient.getInstance().getPlayer();
        switch (keyCode) {
            case NativeKeyEvent.VC_PAUSE -> player.playOrPauseWithFade();
            case NativeKeyEvent.VC_INSERT -> player.setVolume(player.getVolume() + 0.05);
            case NativeKeyEvent.VC_DELETE -> player.setVolume(player.getVolume() - 0.05);
            case NativeKeyEvent.VC_PAGE_UP -> player.pre();
            case NativeKeyEvent.VC_PAGE_DOWN -> player.next();
            case 3658 -> snapshot();
        }
    }

    private void snapshot() {
        Thread.startVirtualThread(() -> {
            WinDef.HWND gameHWND = GameAppListener.getInstance().getGameHWND();
            BufferedImage bufferedImage = WindowCapture.captureGameClientArea(gameHWND);
            Path dir = Path.of("snapshot");
            try {
                Files.createDirectories(dir);
                ImageIO.write(bufferedImage, "png", dir.resolve(System.currentTimeMillis() + ".png").toFile());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
    }

    public void nativeKeyReleased(NativeKeyEvent e) {

    }

    public void nativeKeyTyped(NativeKeyEvent e) {

    }


    public Optional<String> getForegroundWindowTitle() {
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        if (hwnd == null) {
            return Optional.empty();
        }
        char[] windowText = new char[512];
        User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
        String currentTitle = Native.toString(windowText);
        return Optional.of(currentTitle);
    }

}
