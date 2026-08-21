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


    public ObservableList<Player> getHistoryPlayers() {
        return historyPlayers;
    }
}
