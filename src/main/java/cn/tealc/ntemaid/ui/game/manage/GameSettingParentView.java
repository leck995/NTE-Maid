package cn.tealc.ntemaid.ui.game.manage;

import atlantafx.base.util.Animations;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @description:
 * @author: Leck
 * @create: 2025-02-10 19:17
 */
public class GameSettingParentView implements FxmlView<GameSettingParentViewModel>, Initializable {
    @InjectViewModel
    private GameSettingParentViewModel viewModel;
    @FXML
    private StackPane root;
    @FXML
    private HBox headerPane;
    @FXML
    private StackPane content;
    @FXML
    private ToggleGroup childSelectedToggle;

    private Parent advanceChild;
    private Parent baseChild;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createBaseChild();
    }


    @FXML
    void toAdvanceChild(ActionEvent event) {
//        if (event.getSource() instanceof ToggleButton toggleButton){
//            if (toggleButton.isSelected()) {
//                if (advanceChild == null) {
//                    ViewTuple<GameAdvanceSettingView, GameAdvanceSettingViewModel> viewTuple = FluentViewLoader.fxmlView(GameAdvanceSettingView.class).load();
//                    advanceChild = viewTuple.getView();
//                }
//                content.getChildren().setAll(advanceChild);
//                Animations.slideInUp(advanceChild, Duration.millis(300)).play();
//            } else {
//                toggleButton.setSelected(true);
//            }
//        }
    }



    @FXML
    void toBaseChild(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton toggleButton){
            if (toggleButton.isSelected()) {
                createBaseChild();
                Animations.slideInUp(baseChild, Duration.millis(300)).play();
            } else {
                toggleButton.setSelected(true);
            }
        }
    }

    private void createBaseChild(){
        if (baseChild == null) {
            ViewTuple<GameBaseSettingView, GameBaseSettingViewModel> viewTuple = FluentViewLoader.fxmlView(GameBaseSettingView.class).load();
            baseChild = viewTuple.getView();
        }
        content.getChildren().setAll(baseChild);
        baseChild.toFront();
    }

}