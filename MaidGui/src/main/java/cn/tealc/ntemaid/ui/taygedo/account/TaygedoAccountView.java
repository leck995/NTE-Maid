package cn.tealc.ntemaid.ui.taygedo.account;

import atlantafx.base.theme.Styles;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import com.jfoenixN.controls.JFXDialogLayout;
import de.saxsys.mvvmfx.*;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TaygedoAccountView implements FxmlView<TaygedoAccountViewModel>, Initializable {
    private static final Logger LOG= LoggerFactory.getLogger(TaygedoAccountView.class);
    @InjectViewModel
    private TaygedoAccountViewModel viewModel;
    @FXML
    private ListView<TaygedoAccount> accountListView;
    @FXML
    private Button addUserBtn;
    @FXML
    private VBox emptyPane;
    @FXML
    private AnchorPane contentPane;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        contentPane.visibleProperty().bind(Bindings.isNotEmpty(viewModel.getAccountList()));
        contentPane.managedProperty().bind(Bindings.isNotEmpty(viewModel.getAccountList()));
        emptyPane.visibleProperty().bind(Bindings.isEmpty(viewModel.getAccountList()));
        emptyPane.managedProperty().bind(Bindings.isEmpty(viewModel.getAccountList()));


        addUserBtn.setTranslateY(-45);
        accountListView.setItems(viewModel.getAccountList());
        accountListView.setCellFactory((ListView<TaygedoAccount> listView) -> new AccountCell());
    }

    @FXML
    void addUser(ActionEvent event) {
        ViewTuple<TaygedoLoginView, TaygedoLoginViewModel> viewTuple = FluentViewLoader.fxmlView(TaygedoLoginView.class).load();
        MvvmFX.getNotificationCenter().publish(NotificationKey.DIALOG, viewTuple.getView(),viewTuple.getCodeBehind());
    }

    class AccountCell extends ListCell<TaygedoAccount> {
        private final Label phoneLabel = new Label();
        private final Label nameLabel = new Label();
        private final Label index = new Label();
        private final Button delete = new Button(null, new FontIcon(Material2AL.DELETE));

        public AccountCell() {
            phoneLabel.getStyleClass().add("user-label");
            nameLabel.getStyleClass().add("role-label");
            delete.setVisible(false);
            delete.getStyleClass().add("delete-btn");
            delete.setOnAction(event -> {
                if (getItem() != null) {
                    delete();
                }
            });

            VBox vbox = new VBox(3.0, phoneLabel, nameLabel);
            vbox.setAlignment(Pos.CENTER_LEFT);

            HBox hbox = new HBox(10.0, index, vbox, delete);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            hbox.getStyleClass().add("user");
            hbox.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
            hbox.setAlignment(Pos.CENTER_LEFT);
            setGraphic(hbox);
        }

        @Override
        protected void updateItem(TaygedoAccount account, boolean b) {
            super.updateItem(account, b);
            if (!b) {
                index.setText(String.valueOf(getIndex() + 1));
                phoneLabel.setText("手机号: " + account.getPhone());
                nameLabel.setText("昵称: " + account.getName());
                delete.setVisible(true);
            } else {
                index.setText(null);
                phoneLabel.setText(null);
                nameLabel.setText(null);
                delete.setVisible(false);
            }
        }

        private void delete() {
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            Label title = new Label("确认");
            title.getStyleClass().add(Styles.TITLE_3);
            dialogLayout.setHeading(title);
            Label content = new Label(String.format("确认删除账号 %s 的数据吗", getItem().getPhone()));
            dialogLayout.setBody(content);
            Button saveBtn = new Button("确认");
            saveBtn.getStyleClass().add(Styles.ACCENT);
            Button cancelBtn = new Button("取消");
            cancelBtn.setCancelButton(true);

            saveBtn.setOnAction(event1 -> {
                viewModel.deleteAccount(getIndex(), getItem());
            });
            dialogLayout.setActions(saveBtn, cancelBtn);
            MvvmFX.getNotificationCenter().publish(NotificationKey.DIALOG, dialogLayout);
        }
    }


}