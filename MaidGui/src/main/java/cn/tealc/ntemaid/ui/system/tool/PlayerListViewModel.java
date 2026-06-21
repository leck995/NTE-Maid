package cn.tealc.ntemaid.ui.system.tool;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.AppRuntimeData;
import cn.tealc.ntemaid.model.game.Player;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PlayerListViewModel implements ViewModel {
    private ObservableList<Player> players;

    public void initialize(){
        players = AppInjector.getInstance(AppRuntimeData.class).getHistoryPlayers();
        players.add(new Player(1111231231,"名字"));
        players.add(new Player(2111231231,"名字1"));

    }

    public ObservableList<Player> getPlayers() {
        return players;
    }
}
