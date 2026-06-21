package cn.tealc.ntemaid.ui.system.tool;

import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.model.game.Player;
import cn.tealc.teafx.utils.message.MessageInfo;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.MvvmFX;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.net.URL;
import java.util.ResourceBundle;

public class PlayerListView implements FxmlView<PlayerListViewModel>, Initializable {

    @InjectViewModel
    private PlayerListViewModel viewModel;

    @FXML
    private ListView<Player> playerlist;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playerlist.setItems(viewModel.getPlayers());
        playerlist.setPlaceholder(new Label("当前无数据"));
        playerlist.setCellFactory(lv -> new PlayerCell());
    }

    class PlayerCell extends ListCell<Player> {
        private final Label index = new Label();
        private final Label nameLabel = new Label();
        private final Label idLabel = new Label();
        private final Button copyBtn = new Button(null, new FontIcon(Material2AL.CONTENT_COPY));

        public PlayerCell() {
            nameLabel.getStyleClass().add("user-label");
            idLabel.getStyleClass().add("role-label");
            copyBtn.setVisible(false);
            copyBtn.getStyleClass().addAll(Styles.FLAT);
            copyBtn.getStyleClass().add("copy-btn");
            copyBtn.setOnAction(e -> {
                if (getItem() != null) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(getItem().getId()));
                    Clipboard.getSystemClipboard().setContent(content);
                    MvvmFX.getNotificationCenter().publish(
                            NotificationKey.MESSAGE,
                            MessageInfo.success("已复制用户ID: " + getItem().getId()));
                }
            });

            VBox vbox = new VBox(3.0, nameLabel, idLabel);
            vbox.setAlignment(Pos.CENTER_LEFT);

            HBox hbox = new HBox(10.0, index, vbox, copyBtn);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            hbox.getStyleClass().add("user");
            hbox.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
            hbox.setAlignment(Pos.CENTER_LEFT);
            setGraphic(hbox);
        }

        @Override
        protected void updateItem(Player player, boolean empty) {
            super.updateItem(player, empty);
            if (!empty) {
                index.setText(String.valueOf(getIndex() + 1));
                nameLabel.setText(player.getName());
                idLabel.setText(String.valueOf(player.getId()));
                copyBtn.setVisible(true);
            } else {
                index.setText(null);
                nameLabel.setText(null);
                idLabel.setText(null);
                copyBtn.setVisible(false);
            }
        }
    }
}
