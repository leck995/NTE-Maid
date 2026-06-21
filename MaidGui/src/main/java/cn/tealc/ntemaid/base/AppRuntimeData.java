package cn.tealc.ntemaid.base;

import cn.tealc.ntemaid.model.game.Player;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AppRuntimeData {
    private final List<Player> historyPlayers = new ArrayList<>();

    public List<Player> getHistoryPlayers() {
        return historyPlayers;
    }
}
