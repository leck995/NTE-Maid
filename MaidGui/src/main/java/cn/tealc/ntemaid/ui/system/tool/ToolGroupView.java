package cn.tealc.ntemaid.ui.system.tool;

import cn.tealc.ntemaid.ui.base.AbstractGroupView;
import cn.tealc.ntemaid.ui.game.record.GameRecordParentViewModel;
import cn.tealc.ntemaid.ui.game.record.GameTimeView;

public class ToolGroupView extends AbstractGroupView<GameRecordParentViewModel> {

    public ToolGroupView() {
        super("ui.tool.parent.title");
        addTab("ui.tool.parent.tab.title01", PlayerListView.class, true);
    }
}
