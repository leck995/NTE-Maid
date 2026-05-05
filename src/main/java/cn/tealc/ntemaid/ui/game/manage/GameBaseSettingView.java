package cn.tealc.ntemaid.ui.game.manage;

import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.Config;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.util.GameResourcesManager;
import cn.tealc.ntemaid.util.LanguageManager;
import cn.tealc.ntemaid.util.GameClientType;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class GameBaseSettingView implements FxmlView<GameBaseSettingViewModel>, Initializable {
    @InjectViewModel
    private GameBaseSettingViewModel viewModel;
    @FXML
    private TextField gameDirField;
    @FXML
    private ToggleGroup gameSourceTypeToggleGroup;
    @FXML
    private TextField gameStartAppField;
    @FXML
    private StackPane gameStartAppGroup;
    @FXML
    private RadioButton gameStartAppRadioDefault;
    @FXML
    private ToggleGroup gameStartAppType;
    @FXML
    private RadioButton sourceTypeBtn01;
    @FXML
    private RadioButton sourceTypeBtn02;
    @FXML
    private RadioButton sourceTypeBtn04;
    @FXML
    private ListView<String> paramListView;
    @FXML
    private TextField paramField;
    @FXML
    private ToggleGroup gameDxToggleGroup;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        viewModel.init();
        initGameAsset();
        initStartParam();
    }


    private void initGameAsset(){
        gameDirField.setEditable(false);
        gameDirField.textProperty().bindBidirectional(viewModel.gameDirProperty());

        viewModel.gameClientTypeProperty().addListener((observable, oldValue, newValue) -> {
            updateSelectedSourceType(newValue);
        });
        updateSelectedSourceType(viewModel.getGameClientType());

/*        gameSourceTypeToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == gameSourceTypeToggleGroup.getToggles().get(0)) {
                //viewModel.setLocalSourceType(SourceType.DEFAULT);
                boolean success = viewModel.changeServer(SourceType.DEFAULT);
                if (success) {
                    gameStartAppField.setDisable(false);
                }


            }else if(newValue == gameSourceTypeToggleGroup.getToggles().get(1)) {
                //viewModel.setLocalSourceType(SourceType.BILIBILI);
            }else if(newValue == gameSourceTypeToggleGroup.getToggles().get(2)) {
                //viewModel.setLocalSourceType(SourceType.WE_GAME);
            }else if(newValue == gameSourceTypeToggleGroup.getToggles().get(3)) {
                //viewModel.setLocalSourceType(SourceType.GLOBAL);
            }
        });*/
        gameStartAppField.textProperty().bindBidirectional(viewModel.gameAppStartPathProperty());
        gameStartAppGroup.disableProperty().bind(gameStartAppType.selectedToggleProperty().isEqualTo(gameStartAppRadioDefault));

        if (!viewModel.isGameAppStartCustom()){
            gameStartAppType.selectToggle(gameStartAppType.getToggles().getFirst());
        }else {
            gameStartAppType.selectToggle(gameStartAppType.getToggles().get(1));
        }
    }

    private void initStartParam(){
        paramField.setOnAction(event -> {
            String param = paramField.getText();
            if (!param.trim().isEmpty()){
                viewModel.addParam(paramField.getText());
                paramField.clear();
            }
        });
        paramListView.setItems(viewModel.getStartUpParams());
        paramListView.setCellFactory(stringListView -> new ParamListCell());

        if (viewModel.isDx11()){
            gameDxToggleGroup.selectToggle(gameDxToggleGroup.getToggles().getFirst());
        }else if (viewModel.isDx12()){
            gameDxToggleGroup.selectToggle(gameDxToggleGroup.getToggles().getLast());
        }
    }





    private void updateSelectedSourceType(GameClientType sourceType) {
        if (sourceType  == GameClientType.DEFAULT) {
            gameSourceTypeToggleGroup.selectToggle(gameSourceTypeToggleGroup.getToggles().get(0));
        }
        else if (sourceType  == GameClientType.BILIBILI) {
            gameSourceTypeToggleGroup.selectToggle(gameSourceTypeToggleGroup.getToggles().get(1));
        }
        else if(sourceType  == GameClientType.GLOBAL) {
            gameSourceTypeToggleGroup.selectToggle(gameSourceTypeToggleGroup.getToggles().get(2));
        }
    }


    @FXML
    void setDX(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof RadioButton button) {
            switch (button.getText()) {
                case "DX11" -> {
                    viewModel.addDx11();
                }
                case "DX12" -> {
                    viewModel.addDx12();
                }
            }
        }
    }

    @FXML
    void setGameDir(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(LanguageManager.getString("ui.setting.file.game_dir.title"));
        File file = directoryChooser.showDialog(gameDirField.getScene().getWindow());
        if (file != null) {
            File startApp = new File(file.getAbsolutePath() + File.separator + "NTELauncher.exe");
            if (startApp.exists()) {
                gameDirField.setText(file.getAbsolutePath());
            }else {
                NotificationManager.publish(NotificationKey.MESSAGE, MessageInfo.warning(LanguageManager.getString("ui.setting.message.01")));
            }
        }
    }

    @FXML
    void setSelectedGameType(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof RadioButton button) {
            switch (button.getAccessibleText()) {
                case "default"-> {
                    viewModel.setGameClientType(GameClientType.DEFAULT);
                }
                case "bilibili" -> {
                    viewModel.setGameClientType(GameClientType.BILIBILI);
                }
                case "global" -> {
                    viewModel.setGameClientType(GameClientType.GLOBAL);
                }
            }
        }
    }

    @FXML
    void setAppPathModel(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof RadioButton button) {
            switch (button.getAccessibleText()) {
                case "default" -> {
                    Config.setting.setGameStartAppCustom(false);
                    File gameExeClient = GameResourcesManager.getGameExeBase();
                    if (gameExeClient != null) {
                        gameStartAppField.setText(gameExeClient.getAbsolutePath());
                    }else {
                        gameStartAppField.setText("NTELauncher.exe");
                    }
                    gameStartAppField.positionCaret(gameStartAppField.getText().length());
                }
                case "custom" -> {
                    Config.setting.setGameStartAppCustom(true);
                }
            }
        }
    }
    @FXML
    void setGameApp(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LanguageManager.getString("ui.setting.file.app.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("exe","*.exe","*.*"));
        File file = fileChooser.showOpenDialog(gameDirField.getScene().getWindow());
        if (file != null) {
            gameStartAppField.setText(file.getAbsolutePath());
        }
    }


/*    void setSelectedGameType(ActionEvent event) {
        Button cancelBtn = ButtonBuilder.create().title("取消")
                .cancel().build();
        Button okBtn = ButtonBuilder.create().title("确定")
                .styleClass(Styles.DANGER)
                .ok()
                .action(actionEvent -> {
                    Object source = event.getSource();
                    if (source instanceof RadioButton button) {

                    }
                    cancelBtn.fire();
                }).build();
        JFXDialogLayout layout = DialogBuilder.create()
                .title("提示")
                .message("请确认选取的区服正确，错误的区服将会影响游戏的正确更新")
                .buttons(okBtn, cancelBtn)
                .build();
        NotificationManager.dialog(layout);
    }*/

    class ParamListCell extends ListCell<String>{
        private final Button btn;
        private final StackPane child;
        private final Label row;
        public ParamListCell() {
            child = new StackPane();
            row = new Label();
            btn = new Button(null,new FontIcon(Material2AL.DELETE_OUTLINE));
            btn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "delete-btn");
            btn.setOnAction(event -> viewModel.deleteParam(getIndex()));
            child.getChildren().addAll(row,btn);
            StackPane.setAlignment(row, Pos.CENTER_LEFT);
            StackPane.setAlignment(btn, Pos.CENTER_RIGHT);


        }



        @Override
        protected void updateItem(String string, boolean b) {
            super.updateItem(string, b);
            if (!b){
                setGraphic(child);
                row.setText(string);
            }else {
                setGraphic(null);
                row.setText(null);
            }
        }
    }
}
