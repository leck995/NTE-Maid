package cn.tealc.ntemaid.ui.taygedo.account;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 塔吉多验证码登录界面
 * 绑定 TaygedoLoginViewModel，处理用户输入和按钮交互
 */
public class TaygedoLoginView implements FxmlView<TaygedoLoginViewModel>, Initializable {

    @InjectViewModel
    private TaygedoLoginViewModel viewModel;

    @FXML
    private AnchorPane root;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField captchaField;
    @FXML
    private Button sendCaptchaBtn;
    @FXML
    private Button loginBtn;
    @FXML
    private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 双向绑定输入框
        viewModel.phoneProperty().bindBidirectional(phoneField.textProperty());
        viewModel.captchaProperty().bindBidirectional(captchaField.textProperty());

        // 按钮配置：loading中或倒计时中禁用
        sendCaptchaBtn.disableProperty().bind(
                viewModel.loadingProperty().or(viewModel.canSendCaptchaProperty().not()));
        loginBtn.disableProperty().bind(viewModel.loadingProperty());

        // 获取验证码按钮的倒计时文本
        sendCaptchaBtn.textProperty().bind(viewModel.countdownTextProperty());

        // 状态消息更新时，成功则显示为绿色
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        viewModel.loginSuccessProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                statusLabel.setStyle("-fx-text-fill: green;");
            }
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
}
