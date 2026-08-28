package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.model.game.Player;
import com.google.inject.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AppRuntimeData {
    private final ObservableList<Player> historyPlayers = FXCollections.observableArrayList();

    /** 塔吉多 token 是否已完成刷新（启动时刷新一次后置 true） */
    private volatile boolean taygedoTokenRefreshed = false;


    public ObservableList<Player> getHistoryPlayers() {
        return historyPlayers;
    }

    /** 塔吉多 token 是否已刷新完成 */
    public boolean isTaygedoTokenRefreshed() { return taygedoTokenRefreshed; }

    /** 标记塔吉多 token 刷新完成 */
    public void setTaygedoTokenRefreshed(boolean taygedoTokenRefreshed) {
        this.taygedoTokenRefreshed = taygedoTokenRefreshed;
    }
}
