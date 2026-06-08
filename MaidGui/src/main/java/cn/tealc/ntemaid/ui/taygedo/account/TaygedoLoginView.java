package cn.tealc.ntemaid.ui.taygedo.account;

import cn.tealc.ntemaid.ui.component.BaseDialog;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class TaygedoLoginView extends BaseDialog implements FxmlView<TaygedoLoginViewModel>, Initializable {

    @InjectViewModel
    private TaygedoLoginViewModel viewModel;

    @FXML
    private AnchorPane root;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField captchaField;
    @FXML
    private Button sendCaptchaBtn;
    @FXML
    private Button captchaLoginBtn;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button passwordLoginBtn;
    @FXML
    private Hyperlink modeSwitchLink;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox captchaPane;
    @FXML
    private VBox passwordPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel.phoneProperty().bindBidirectional(phoneField.textProperty());
        viewModel.captchaProperty().bindBidirectional(captchaField.textProperty());
        viewModel.passwordProperty().bindBidirectional(passwordField.textProperty());

        sendCaptchaBtn.disableProperty().bind(
                viewModel.loadingProperty().or(viewModel.canSendCaptchaProperty().not()));
        captchaLoginBtn.disableProperty().bind(viewModel.loadingProperty());
        passwordLoginBtn.disableProperty().bind(viewModel.loadingProperty());

        sendCaptchaBtn.textProperty().bind(viewModel.countdownTextProperty());

        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        viewModel.loginSuccessProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                statusLabel.setStyle("-fx-text-fill: green;");
                closeDialog();
            }
        });

        viewModel.passwordModeProperty().addListener((obs, old, passwordMode) -> {
            captchaPane.setVisible(!passwordMode);
            captchaPane.setManaged(!passwordMode);
            passwordPane.setVisible(passwordMode);
            passwordPane.setManaged(passwordMode);

            titleLabel.setText(passwordMode ? "塔吉多密码登录" : "塔吉多验证码登录");
            modeSwitchLink.setText(passwordMode ? "验证码登录" : "密码登录");
        });
    }

    @FXML
    private void onSendCaptcha(ActionEvent event) {
        viewModel.sendCaptcha();
    }

    @FXML
    private void onLogin(ActionEvent event) {
        viewModel.login();
    }

    @FXML
    private void onToggleMode(ActionEvent event) {
        viewModel.toggleMode();
    }
}
