package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.util.Animations;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class GameGachaGroupView implements FxmlView<GameGachaGroupViewModel>, Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(GameGachaGroupView.class);
    @InjectViewModel
    private GameGachaGroupViewModel viewModel;
    @FXML
    private StackPane content;
    private Parent commonChild;
    private Parent taygedoView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createCommonChild();
        //createTaygedoChild();
    }


    @FXML
    void toCommonChild(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton toggleButton) {
            if (toggleButton.isSelected()) {
                createCommonChild();
            } else {
                toggleButton.setSelected(true);
            }
        }
    }



    private void createCommonChild() {
        if (commonChild == null) {
            ViewTuple<GameGachaCommonView, GameGachaCommonViewModel> viewTuple =
                    FluentViewLoader
                            .fxmlView(GameGachaCommonView.class)
                            .load();
            commonChild = viewTuple.getView();
            commonChild.setOpacity(0);
            content.getChildren().setAll(commonChild);
            Platform.runLater(() -> {
                commonChild.setOpacity(1);
                Animations.slideInUp(commonChild, Duration.millis(300)).play();
            });
        } else {
            content.getChildren().setAll(commonChild);
        }
    }

    @FXML
    void toTaygedoViewView(ActionEvent event) {
        if (event.getSource() instanceof ToggleButton toggleButton) {
            if (toggleButton.isSelected()) {
             createTaygedoChild();
            } else {
                toggleButton.setSelected(true);
            }
        }
    }

    private void createTaygedoChild() {
        if (taygedoView == null) {
            ViewTuple<GameGachaView, GameGachaViewModel> viewTuple =
                    FluentViewLoader
                            .fxmlView(GameGachaView.class)
                            .load();
            taygedoView = viewTuple.getView();
            taygedoView.setOpacity(0);
            content.getChildren().setAll(taygedoView);
            Platform.runLater(() -> {
                taygedoView.setOpacity(1);
                Animations.slideInUp(taygedoView, Duration.millis(300)).play();
            });
        } else {
            content.getChildren().setAll(taygedoView);
        }
    }
}