package cn.tealc.ntemaid.ui.game.music;

import atlantafx.base.controls.ToggleSwitch;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.ui.component.BaseDialog;
import de.saxsys.mvvmfx.FxmlView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MusicSettingView extends BaseDialog implements FxmlView<MusicSettingViewModel>, Initializable {
    @FXML
    private ToggleSwitch gamePlayerSwitch;

    @FXML
    private StackPane root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePlayerSwitch.selectedProperty().bindBidirectional(Config.getSetting().gamePlayerOpenProperty());
    }
}
