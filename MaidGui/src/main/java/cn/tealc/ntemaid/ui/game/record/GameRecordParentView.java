package cn.tealc.ntemaid.ui.game.record;

import cn.tealc.ntemaid.ui.base.AbstractGroupView;

public class GameRecordParentView extends AbstractGroupView<GameRecordParentViewModel> {

    public GameRecordParentView() {
        super("ui.game_statistics.title");
        addTab("ui.game_statistics.nav.button03", GameTimeView.class, true);
    }
}
