package cn.tealc.ntemaid.ui.system.tool;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.AppRuntimeData;
import cn.tealc.ntemaid.model.game.Player;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.ObservableList;

public class PlayerListViewModel implements ViewModel {
    private ObservableList<Player> players;

    public void initialize(){
        players = AppInjector.getInstance(AppRuntimeData.class).getHistoryPlayers();
    }

    public ObservableList<Player> getPlayers() {
        return players;
    }
}
