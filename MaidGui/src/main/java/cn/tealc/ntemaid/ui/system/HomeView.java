package cn.tealc.ntemaid.ui.system;

import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.jna.GameAppListener;
import cn.tealc.ntemaid.util.GameResourcesManager;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.ntemaid.vision.MapRegionConfig;
import cn.tealc.ntemaid.vision.RouteResult;
import cn.tealc.teafx.utils.message.MessageInfo;
import com.sun.jna.platform.win32.WinDef;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.MvvmFX;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @description:
 * @author: Leck
 * @create: 2024-07-03 19:57
 */
public class HomeView implements Initializable, FxmlView<HomeViewModel> {
    @InjectViewModel
    private HomeViewModel viewModel;
    @FXML
    private BorderPane root;
    @FXML
    private Button gameTimeBtn;
    @FXML
    private Button startGameBtn;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startGameBtn.disableProperty().bind(viewModel.startGameBtnDisabledProperty());
        Tooltip gameTimeTip = new Tooltip();
        gameTimeTip.textProperty().bind(viewModel.gameTimeTipTextProperty());
        gameTimeBtn.setTooltip(gameTimeTip);
        gameTimeBtn.textProperty().bind(viewModel.gameTimeTextProperty());
        setChangeBgEnable();
        viewModel.checkIsWeekEnd();
    }


    /**
     * @return void
     * @description: 启动切换背景
     * @param:
     * @date: 2025/2/18
     */
    private void setChangeBgEnable() {
        if (Config.getSetting().getDiyHomeBgType() == 2) {
            root.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    MvvmFX.getNotificationCenter().publish(NotificationKey.CHANGE_BG);
                }
            });
        }
    }


    @FXML
    void startGame(ActionEvent event) {
        viewModel.startGame();
    }

    @FXML
    void showGameTimerAlert(ActionEvent event) {
 /*       PlayTimeAlertItemView view = new PlayTimeAlertItemView();
        MvvmFX.getNotificationCenter().publish(NotificationKey.DIALOG, view);*/
    }


    @FXML
    void startUpdate(ActionEvent event) {
        /*     viewModel.startUpdate();*/
    }


    @FXML
    void toAlbumDir(ActionEvent event) {
        Optional<File> fileOp = GameResourcesManager.getGameScreenShoot();
        if (fileOp.isPresent()) {
            try {
                Desktop.getDesktop().open(fileOp.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.home.message.type06")));
        }
    }

    @FXML
    void toGameDir(ActionEvent event) {
        Optional<File> optional = GameResourcesManager.getGameDir();
        if (optional.isPresent()) {
            try {
                Desktop.getDesktop().open(optional.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.home.message.type04")));
        }
    }
}