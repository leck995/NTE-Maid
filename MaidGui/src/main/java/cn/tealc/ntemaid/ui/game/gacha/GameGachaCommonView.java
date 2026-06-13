package cn.tealc.ntemaid.ui.game.gacha;

import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class GameGachaCommonView implements FxmlView<GameGachaCommonViewModel>, Initializable {
    @InjectViewModel
    private GameGachaCommonViewModel viewModel;

    @FXML
    private ComboBox<?> accountCombo;

    @FXML
    private ImageView avatarView;

    @FXML
    private VBox contentPane;

    @FXML
    private VBox emptyPane;

    @FXML
    private Label levelLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label luckLabel;

    @FXML
    private HBox playerInfoPane;

    @FXML
    private HBox poolCardsPane;

    @FXML
    private Button refreshBtn;

    @FXML
    private Label roleNameLabel;

    @FXML
    private AnchorPane root;

    @FXML
    private Label statusLabel;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void onRefresh(ActionEvent event) {

    }


    @FXML
    void importGachaJsonData(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择抽卡数据");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("抽卡数据文件","json"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file.exists()){
            JFXDialogLayout layout = new JFXDialogLayout();
            Label title = new Label("设置游戏ID");
            title.getStyleClass().add(Styles.TITLE_2);
            layout.setHeading(title);

            TextField field = new TextField();
            field.setPromptText("输入玩家ID");
            Label label = new Label("请正确输入玩家ID，如果输错了可能会影响后续记录保存");
            label.getStyleClass().add(Styles.TEXT_SUBTLE);
            VBox box = new VBox(8.0,field,label);
            layout.setBody(box);

            Button ok = new Button("确认");
            ok.getStyleClass().add(Styles.ACCENT);
            ok.setOnAction(e -> {

            });
            Button cancel = new Button("取消");
            cancel.setCancelButton(true);
            layout.setActions(ok,cancel);
            NotificationManager.dialog(layout);

        }

    }


}
