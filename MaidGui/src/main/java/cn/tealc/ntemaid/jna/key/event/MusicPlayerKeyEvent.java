package cn.tealc.ntemaid.jna.key.event;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.key.GlobalKeyListenManager;
import cn.tealc.ntemaid.player.BaseAudioPlayer;
import cn.tealc.ntemaid.player.MusicPlayerClient;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.util.Set;


public class MusicPlayerKeyEvent implements KeyEvent{
    private final BaseAudioPlayer player;

    public MusicPlayerKeyEvent() {
        player = MusicPlayerClient.getInstance().getPlayer();
    }

    @Override
    public void accept(int keyCode, GlobalKeyListenManager manager) {
        switch (keyCode) {
            case NativeKeyEvent.VC_PAUSE -> player.playOrPauseWithFade();
            case NativeKeyEvent.VC_INSERT -> player.setVolume(player.getVolume() + 0.05);
            case NativeKeyEvent.VC_DELETE -> player.setVolume(player.getVolume() - 0.05);
            case NativeKeyEvent.VC_PAGE_UP -> player.pre();
            case NativeKeyEvent.VC_PAGE_DOWN -> player.next();
            case 3658,NativeKeyEvent.VC_MINUS  -> removePlayerListener();
        }
    }
    public void removePlayerListener(){
        Config.getSetting().setMusicEnable(!Config.getSetting().isMusicEnable());
    }
}
