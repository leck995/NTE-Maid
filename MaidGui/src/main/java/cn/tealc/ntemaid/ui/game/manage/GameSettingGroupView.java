package cn.tealc.ntemaid.ui.game.manage;

import cn.tealc.ntemaid.ui.base.AbstractGroupView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.scene.control.ToggleButton;

public class GameSettingGroupView extends AbstractGroupView<GameSettingGroupViewModel> {

    @InjectViewModel
    private GameSettingGroupViewModel viewModel;

    public GameSettingGroupView() {
        super("ui.game_manager.base.title");

        ToggleButton baseBtn = addTab("ui.game_manager.base.tab.title01", GameBaseSettingView.class, true);
        ToggleButton advanceBtn = addTab("ui.game_manager.base.tab.title02", GameAdvanceSettingView.class, false);
        addTab("ui.game_manager.base.tab.title03", GameEnhanceView.class, false);

        advanceBtn.setVisible(false);
        advanceBtn.setManaged(false);
    }
}
