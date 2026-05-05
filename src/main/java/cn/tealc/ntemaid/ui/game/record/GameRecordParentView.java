package cn.tealc.ntemaid.ui.game.record;

import atlantafx.base.util.Animations;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @description:
 * @author: Leck
 * @create: 2024-11-16 20:47
 */
public class GameRecordParentView implements FxmlView<GameRecordParentViewModel>, Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(GameRecordParentView.class);
    @FXML
    private StackPane content;
    @FXML
    private AnchorPane root;
    private Parent timeView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createTime();
    }

    @FXML
    void toMain(ActionEvent event) {
        content.getChildren().clear();
    }

    @FXML
    void toRecord(ActionEvent event) {

    }

    @FXML
    void toTime(ActionEvent event) {
        ToggleButton toggleButton= (ToggleButton) event.getSource();
        if (toggleButton.isSelected()){
            createTime();
            Animations.slideInUp(timeView, Duration.millis(300)).play();
        }else {
            toggleButton.setSelected(true);
        }
    }

    private void createTime(){
        if (timeView == null) {
            ViewTuple<GameTimeView, GameTimeViewModel> viewTuple = FluentViewLoader.fxmlView(GameTimeView.class).load();
            timeView = viewTuple.getView();
        }
        content.getChildren().setAll(timeView);
    }


}