package cn.tealc.ntemaid.ui.taygedo.signin;

import atlantafx.base.controls.ToggleSwitch;
import cn.tealc.ntemaid.base.notification.NotificationKey;
import cn.tealc.ntemaid.base.notification.NotificationManager;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.model.SigninReward;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.net.URL;
import java.util.ResourceBundle;

public class TaygedoSignInView implements FxmlView<TaygedoSignInViewModel>, Initializable {

    @InjectViewModel
    private TaygedoSignInViewModel viewModel;

    @FXML
    private ComboBox<TaygedoAccount> accountCombo;
    @FXML
    private Label statusLabel;
    @FXML
    private Button signInBtn;
    @FXML
    private Button signInAllBtn;
    @FXML
    private ToggleSwitch autoSignSwitch;
    @FXML
    private VBox contentPane;
    @FXML
    private VBox emptyPane;
    @FXML
    private FlowPane rewardPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountCombo.setItems(viewModel.getAccountList());
        accountCombo.setCellFactory(param -> new AccountListCell());
        accountCombo.setButtonCell(new AccountListCell());
        viewModel.selectedAccountProperty().bindBidirectional(accountCombo.valueProperty());

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        signInBtn.disableProperty().bind(viewModel.getSignInCommand().executableProperty().not());
        signInAllBtn.disableProperty().bind(viewModel.getSignInAllCommand().executableProperty().not());

        autoSignSwitch.selectedProperty().bindBidirectional(viewModel.autoSignProperty());

        // 无账号时显示空状态提示
        contentPane.visibleProperty().bind(Bindings.isNotEmpty(viewModel.getAccountList()));
        contentPane.managedProperty().bind(Bindings.isNotEmpty(viewModel.getAccountList()));
        emptyPane.visibleProperty().bind(Bindings.isEmpty(viewModel.getAccountList()));
        emptyPane.managedProperty().bind(Bindings.isEmpty(viewModel.getAccountList()));

        viewModel.getRewardList().addListener((javafx.collections.ListChangeListener<SigninReward>) change -> refreshRewards());
        viewModel.signedDaysProperty().addListener((obs, old, val) -> refreshRewards());

        viewModel.initialize();
    }

    private void refreshRewards() {
        rewardPane.getChildren().clear();
        int signed = viewModel.signedDaysProperty().get();
        for (int i = 0; i < viewModel.getRewardList().size(); i++) {
            SigninReward reward = viewModel.getRewardList().get(i);
            boolean claimed = i < signed;
            rewardPane.getChildren().add(createRewardCard(i + 1, reward, claimed));
        }
    }

    private VBox createRewardCard(int day, SigninReward reward, boolean claimed) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(100);
        card.setPrefHeight(120);
        card.getStyleClass().add("reward-card");
        if (claimed) {
            card.getStyleClass().add("claimed");
        }

        Label dayLabel = new Label("第" + day + "天");
        dayLabel.getStyleClass().add("day-label");

        Label nameLabel = new Label(reward.getName());
        nameLabel.getStyleClass().add("reward-name");

        Label numLabel = new Label("x" + reward.getNum());
        numLabel.getStyleClass().add("reward-num");

        FontIcon icon;
        if (claimed) {
            icon = new FontIcon(Material2AL.CHECK_CIRCLE);
            icon.getStyleClass().add("claimed-icon");
        } else {
            icon = new FontIcon(Material2AL.CHECK_CIRCLE_OUTLINE);
            icon.getStyleClass().add("unclaimed-icon");
        }

        card.getChildren().addAll(dayLabel, icon, nameLabel, numLabel);
        return card;
    }

    @FXML
    void onSignIn(ActionEvent event) {
        viewModel.getSignInCommand().execute();
    }

    @FXML
    void onSignInAll(ActionEvent event) {
        viewModel.getSignInAllCommand().execute();
    }

    @FXML
    void onGoToAccount(ActionEvent event) {
        NotificationManager.publish(NotificationKey.TAYGEDO_ACCOUNT_TAB);
    }

    private static class AccountListCell extends ListCell<TaygedoAccount> {
        @Override
        protected void updateItem(TaygedoAccount item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                setText(item.getPhone() + (item.getName() != null ? " (" + item.getName() + ")" : ""));
            } else {
                setText(null);
            }
        }
    }
}
