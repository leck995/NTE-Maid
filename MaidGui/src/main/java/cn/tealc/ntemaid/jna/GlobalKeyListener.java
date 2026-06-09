package cn.tealc.ntemaid.jna;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.util.Optional;

public class GlobalKeyListener implements NativeKeyListener {
  private static final String GAME_NAME="异环  ";
  public void nativeKeyPressed(NativeKeyEvent e) {
    if (!Config.getSetting().isMusicEnable()){
      return;
    }
    boolean isGameActive = getForegroundWindowTitle()
            .map(t -> t.equals(GAME_NAME))
            .orElse(false);
    if (!isGameActive) {
      return;
    }


    int keyCode = e.getKeyCode();
    BaseAudioPlayer player = MusicPlayerClient.getInstance().getPlayer();
    switch (keyCode) {
      case NativeKeyEvent.VC_PAUSE -> player.playOrPauseWithFade();
      case NativeKeyEvent.VC_INSERT    -> player.setVolume(player.getVolume() + 0.1);
      case NativeKeyEvent.VC_DELETE  -> player.setVolume(player.getVolume() - 0.1);
      case NativeKeyEvent.VC_PAGE_UP  -> player.pre();
      case NativeKeyEvent.VC_PAGE_DOWN -> player.next();
    }
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
