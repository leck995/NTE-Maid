package cn.tealc.ntemaid.ui.game.gacha;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.ui.base.AbstractGroupView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.scene.control.ToggleButton;

public class GameGachaGroupView extends AbstractGroupView<GameGachaGroupViewModel> {

    @InjectViewModel
    private GameGachaGroupViewModel viewModel;

    public GameGachaGroupView() {
        super("ui.account.title");

        ToggleButton taygedoBtn = addTab("ui.gacha.tab.taygedo", GameGachaView.class, false);
        ToggleButton commonBtn = addTab("ui.gacha.tab.common", GameGachaCommonView.class, false);

        if (Config.getSetting().isEnableTaygedo()) {
            selectTab(taygedoBtn);
        } else {
            taygedoBtn.setVisible(false);
            taygedoBtn.setManaged(false);
            selectTab(commonBtn);
        }
    }
}
