package cn.tealc.ntemaid.jna.key.event;

import cn.tealc.ntemaid.FXResourcesLoader;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.jna.key.GlobalKeyListenManager;
import cn.tealc.ntemaid.jna.key.Win32KeySender;
import cn.tealc.ntemaid.snapshot.WindowCapture;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.google.inject.Inject;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class OtherKeyEvent implements KeyEvent {
    private static final Logger log = LoggerFactory.getLogger(OtherKeyEvent.class);


    @Override
    public void accept(int keyCode, GlobalKeyListenManager manager) {
        if (keyCode == NativeKeyEvent.VC_EQUALS || keyCode == 3662) {
            snapshot();
        }
    }

    private void snapshot() {
        if (!Config.getSetting().isSnapshot())
            return;
        Thread.startVirtualThread(() -> {
            WinDef.HWND gameHWND = GameAppListener.getInstance().getGameHWND();
            BufferedImage bufferedImage = WindowCapture.captureGameClientArea(gameHWND);
            Path dir = Path.of("snapshot");
            try {
                Files.createDirectories(dir);
                ImageIO.write(bufferedImage, "png", dir.resolve(System.currentTimeMillis() + ".png").toFile());

                AudioClip clip = new AudioClip(FXResourcesLoader.load("audio/snapshot.mp3"));
                clip.setVolume(0.1);
                clip.play();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
    }

}
