package cn.tealc.ntemaid.jna.key.event;

import cn.tealc.ntemaid.jna.key.GlobalKeyListenManager;

import java.util.Set;

public interface KeyEvent {
    /**
     * 响应按键事件
     * @param keyCode 当前触发事件的按键码
     * @param manager manager
     */
    void accept(int keyCode, GlobalKeyListenManager manager);
}