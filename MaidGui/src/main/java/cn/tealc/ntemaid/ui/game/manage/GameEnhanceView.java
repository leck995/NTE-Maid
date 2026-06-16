package cn.tealc.ntemaid.ui.game.manage;

import atlantafx.base.controls.ToggleSwitch;
import cn.tealc.ntemaid.base.Config;
import de.saxsys.mvvmfx.FxmlView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class GameEnhanceView implements FxmlView<GameEnhanceViewModel>, Initializable {
    @FXML
    private StackPane root;

    @FXML
    private ToggleSwitch adventureManualSwitch;

    @FXML
    private ScrollPane content;

    @FXML
    private ToggleSwitch fishingBaitSwitch;

    @FXML
    private ToggleSwitch fishingFinishSwitch;
    @FXML
    private ToggleSwitch fishingEnableSwitch;
    @FXML
    private ToggleSwitch snapshotEnableSwitch;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFishing();

        adventureManualSwitch.selectedProperty().bindBidirectional(Config.getSetting().adventureManualSkipProperty());
        snapshotEnableSwitch.selectedProperty().bindBidirectional(Config.getSetting().snapshotProperty());

    }

    private void initFishing() {
        fishingEnableSwitch.selectedProperty().bindBidirectional(Config.getSetting().fishingProperty());
        fishingBaitSwitch.selectedProperty().bindBidirectional(Config.getSetting().fishingBaitProperty());
        fishingFinishSwitch.selectedProperty().bindBidirectional(Config.getSetting().fishingFinishProperty());
        fishingBaitSwitch.disableProperty().bind(fishingEnableSwitch.selectedProperty().not());
        fishingFinishSwitch.disableProperty().bind(fishingEnableSwitch.selectedProperty().not());
    }




}
