package cn.tealc.ntemaid.jna.key;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.jna.key.event.KeyEvent;
import cn.tealc.ntemaid.jna.key.event.MusicPlayerKeyEvent;
import cn.tealc.ntemaid.jna.key.event.OtherKeyEvent;
import cn.tealc.ntemaid.jna.key.event.WalkKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalKeyListenManager implements NativeKeyListener {
    private static final Logger log = LoggerFactory.getLogger(GlobalKeyListenManager.class);
    private final List<KeyEvent> eventList = new CopyOnWriteArrayList<>();
    //使用线程安全的 Set 记录当前所有被按住的键
    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
    // 是否是由我们自己代码触发的虚拟按键标记
    private volatile boolean isRobotOperating = false;

    private static GlobalKeyListenManager manager;

    public GlobalKeyListenManager() {
        eventList.add(new MusicPlayerKeyEvent());
        eventList.add(new OtherKeyEvent());
        eventList.add(new WalkKeyEvent());
    }

    public static GlobalKeyListenManager getInstance() {
        if (manager == null) manager = new GlobalKeyListenManager();
        return manager;
    }



    public void setRobotOperating(boolean operating) {
        this.isRobotOperating = operating;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // 如果是自己的 Robot 在按键，直接放行，不影响真实物理按键状态
        if (isRobotOperating) {
            return;
        }

        if (!isGameActive()) return;
        int keyCode = e.getKeyCode();

        if (!pressedKeys.contains(keyCode)) {
            pressedKeys.add(keyCode);
            //log.debug("Key Pressed: " + NativeKeyEvent.getKeyText(keyCode));
        }

        eventList.forEach(keyCodeConsumer -> keyCodeConsumer.accept(keyCode, manager));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // 【核心改动】如果是自己的 Robot 在松开按键，绝对不能把物理按键从状态集里删掉！
        if (isRobotOperating) {
            return;
        }

        if (!isGameActive()) return;
        int keyCode = e.getKeyCode();

        pressedKeys.remove(keyCode);
        //log.debug("Key Released: " + NativeKeyEvent.getKeyText(keyCode));
    }
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // 通常不需要处理
    }

    private boolean isGameActive() {
        return getForegroundWindowTitle()
                .map(t -> Config.getSetting().getGameWindowTitles().contains(t))
                .orElse(false);
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

    // 【新加方法】供其他地方或事件子类查询某个键是否正被按住
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }



    public void register(KeyEvent event){
        eventList.add(event);
    }

    public void unregister(KeyEvent event){
        eventList.remove(event);
    }
}